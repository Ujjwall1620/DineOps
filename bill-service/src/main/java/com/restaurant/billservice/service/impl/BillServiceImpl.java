package com.restaurant.billservice.service.impl;

import com.restaurant.billservice.config.BillingConfig;
import com.restaurant.billservice.dto.request.PaymentRequest;
import com.restaurant.billservice.dto.response.BillResponse;
import com.restaurant.billservice.dto.response.PaymentGatewayResponse;
import com.restaurant.billservice.entity.Bill;
import com.restaurant.billservice.entity.BillItem;
import com.restaurant.billservice.entity.PaymentTransaction;
import com.restaurant.billservice.enums.BillStatus;
import com.restaurant.billservice.enums.TransactionStatus;
import com.restaurant.billservice.exception.*;
import com.restaurant.billservice.kafka.consumer.OrderReadyEvent;
import com.restaurant.billservice.kafka.producer.BillProducer;
import com.restaurant.billservice.payment.gateway.PaymentGateway;

import com.restaurant.billservice.payment.strategy.PaymentGatewayFactory;
import com.restaurant.billservice.repository.BillRepository;
import com.restaurant.billservice.service.BillMapper;
import com.restaurant.billservice.service.BillNumberGenerator;
import com.restaurant.billservice.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillServiceImpl implements BillService {

    private final BillRepository        billRepository;
    private final BillMapper            billMapper;
    private final BillNumberGenerator   billNumberGenerator;
    private final BillingConfig         billingConfig;
    private final BillProducer          billProducer;
    private final PaymentGatewayFactory gatewayFactory;

    // ─── Bill Generation ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public BillResponse generateBill(OrderReadyEvent event) {
        log.info("Generating bill for orderId: {}, orderNumber: {}",
                event.getOrderId(), event.getOrderNumber());

        // ── Idempotency guard — mirrors Kitchen Service existsByOrderId() ────────
        if (billRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Bill already exists for orderId: {}. Returning existing.", event.getOrderId());
            return billMapper.toResponse(
                    billRepository.findByOrderId(event.getOrderId())
                            .orElseThrow(() -> new BillNotFoundException(
                                    "Idempotency check passed but bill missing for orderId: "
                                            + event.getOrderId())));
        }

        // ── Build bill shell ─────────────────────────────────────────────────────
        Bill bill = Bill.builder()
                .billNumber(billNumberGenerator.generate())
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .tableNumber(event.getTableNumber())
                .waiterId(event.getWaiterId())
                .waiterName(event.getWaiterName())
                .status(BillStatus.GENERATED)
                .build();

        // ── Map order items → bill items (prices already frozen in event) ────────
        if (event.getItems() != null) {
            event.getItems().stream()
                    .map(payload -> BillItem.builder()
                            .menuItemId(payload.getMenuItemId())
                            .menuItemName(payload.getMenuItemName())
                            .quantity(payload.getQuantity())
                            .pricePerUnit(payload.getPricePerUnit())
                            .subtotal(payload.getSubtotal())
                            .build())
                    .forEach(bill::addItem);
        }

        // ── Tax calculation — 100% config-driven, zero hardcoding ────────────────
        BigDecimal subtotal     = calculateSubtotal(bill);
        BigDecimal gstPct       = billingConfig.getGstPercentage();
        BigDecimal gstAmount    = calculatePercentage(subtotal, gstPct);
        BigDecimal svcChargePct = billingConfig.isServiceChargeEnabled()
                ? billingConfig.getServiceChargePercentage()
                : BigDecimal.ZERO;
        BigDecimal svcChargeAmt = calculatePercentage(subtotal, svcChargePct);
        BigDecimal grandTotal   = subtotal.add(gstAmount).add(svcChargeAmt);

        log.info("Bill calc — subtotal: {}, GST({}%): {}, SvcCharge({}%): {}, grandTotal: {}",
                subtotal, gstPct, gstAmount, svcChargePct, svcChargeAmt, grandTotal);

        bill.setSubtotal(subtotal);
        bill.setGstPercentage(gstPct);
        bill.setGstAmount(gstAmount);
        bill.setServiceChargePct(svcChargePct);
        bill.setServiceChargeAmt(svcChargeAmt);
        bill.setGrandTotal(grandTotal);

        Bill saved = billRepository.save(bill);
        log.info("Bill {} generated, grandTotal: {}", saved.getBillNumber(), saved.getGrandTotal());

        billProducer.publishBillGenerated(saved);
        return billMapper.toResponse(saved);
    }

    // ─── Payment Processing ────────────────────────────────────────────────────

    @Override
    @Transactional
    public BillResponse processPayment(Long billId, PaymentRequest request) {
        log.info("Processing payment for billId: {}, method: {}, idempotencyKey: {}",
                billId, request.getPaymentMethod(), request.getIdempotencyKey());

        Bill bill = findById(billId);

        // ── State machine guard ──────────────────────────────────────────────────
        if (bill.getStatus() == BillStatus.PAID) {
            log.warn("Bill {} already PAID. Returning existing state.", billId);
            return billMapper.toResponse(bill);
        }
        if (bill.getStatus() == BillStatus.CANCELLED
                || bill.getStatus() == BillStatus.REFUNDED) {
            throw new InvalidBillStateException(bill.getStatus(), "payment");
        }
        if (bill.getStatus() == BillStatus.PENDING) {
            throw new InvalidBillStateException(bill.getStatus(),
                    "payment — bill generation not complete");
        }

        // ── Idempotency check ────────────────────────────────────────────────────
        if (request.getIdempotencyKey() != null) {
            boolean keyUsedOnOtherBill = billRepository
                    .findByIdempotencyKey(request.getIdempotencyKey())
                    .map(b -> !b.getId().equals(billId))
                    .orElse(false);
            if (keyUsedOnOtherBill) {
                throw new DuplicatePaymentException(request.getIdempotencyKey());
            }
            bill.setIdempotencyKey(request.getIdempotencyKey());
        }

        // ── Mark as PAYMENT_PENDING (optimistic lock version increments here) ────
        bill.setStatus(BillStatus.PAYMENT_PENDING);
        bill.setPaymentMethod(request.getPaymentMethod());
        billRepository.save(bill);

        // ── Resolve active gateway via Strategy Pattern ──────────────────────────
        PaymentGateway gateway = gatewayFactory.getActiveGateway();

        // Amount always taken from the bill — never from the request body
        request.setAmount(bill.getGrandTotal());
        request.setBillNumber(bill.getBillNumber());
        request.setBillId(bill.getId());

        String txnRef = "TXN-" + bill.getId() + "-" + System.currentTimeMillis();

        // ── Record every attempt for full audit trail ────────────────────────────
        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionRef(txnRef)
                .amount(bill.getGrandTotal())
                .paymentMethod(request.getPaymentMethod())
                .gateway(gateway.getGatewayName())
                .status(TransactionStatus.INITIATED)
                .build();
        bill.addTransaction(transaction);

        // ── Call gateway ─────────────────────────────────────────────────────────
        PaymentGatewayResponse gatewayResponse = gateway.processPayment(request);

        if (gatewayResponse.isSuccess()) {
            log.info("Payment link created for billId: {}, url: {}",
                    billId, gatewayResponse.getPaymentUrl());

            transaction.setStatus(TransactionStatus.SUCCESS);
            transaction.setGatewayTransactionId(gatewayResponse.getGatewayTransactionId());
            transaction.setGatewayResponse(gatewayResponse.getRawResponse());

            // Store the payment URL on the bill so it can be retrieved later
            bill.setPaymentUrl(gatewayResponse.getPaymentUrl());
            bill.setStatus(BillStatus.PAYMENT_PENDING);

            Bill saved = billRepository.save(bill);
            billProducer.publishPaymentCompleted(saved, transaction);
            return billMapper.toResponse(saved);

        } else {
            log.error("Payment FAILED billId: {}, reason: {}",
                    billId, gatewayResponse.getFailureReason());

            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(gatewayResponse.getFailureReason());
            transaction.setGatewayResponse(gatewayResponse.getRawResponse());
            bill.setStatus(BillStatus.FAILED);

            Bill saved = billRepository.save(bill);
            billProducer.publishPaymentFailed(saved, transaction);
            throw new PaymentFailedException(
                    "Payment failed via " + gateway.getGatewayName()
                    + ": " + gatewayResponse.getFailureReason());
        }
    }

    // ─── Read Operations ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillById(Long billId) {
        return billMapper.toResponse(findById(billId));
    }

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillByOrderId(Long orderId) {
        return billMapper.toResponse(
                billRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new BillNotFoundException(
                                "Bill not found for orderId: " + orderId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getAllBills() {
        return billMapper.toResponseList(billRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getBillsByStatus(BillStatus status) {
        return billMapper.toResponseList(billRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getBillsByWaiterId(Long waiterId) {
        return billMapper.toResponseList(billRepository.findByWaiterId(waiterId));
    }

    // ─── Private Helpers ───────────────────────────────────────────────────────

    private Bill findById(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException(billId));
    }

    /**
     * Sums item subtotals — NEVER trusts a pre-computed total.
     * Always recalculates from ground truth item data.
     */
    private BigDecimal calculateSubtotal(Bill bill) {
        return bill.getItems().stream()
                .map(BillItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Generic percentage calculator.
     * calculatePercentage(1000, 18) = 180.00
     * calculatePercentage(1000, 5)  = 50.00
     */
    private BigDecimal calculatePercentage(BigDecimal base, BigDecimal pct) {
        if (pct == null || pct.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return base.multiply(pct)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
