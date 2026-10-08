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
import com.restaurant.billservice.exception.BillNotFoundException;
import com.restaurant.billservice.exception.DuplicatePaymentException;
import com.restaurant.billservice.exception.InvalidBillStateException;
import com.restaurant.billservice.exception.PaymentFailedException;
import com.restaurant.billservice.kafka.consumer.OrderCreatedEvent;
import com.restaurant.billservice.kafka.producer.BillProducer;
import com.restaurant.billservice.payment.gateway.PaymentGateway;
import com.restaurant.billservice.payment.strategy.PaymentGatewayFactory;
import com.restaurant.billservice.repository.BillRepository;
import com.restaurant.billservice.security.JwtUserDetails;
import com.restaurant.billservice.service.BillMapper;
import com.restaurant.billservice.service.BillNumberGenerator;
import com.restaurant.billservice.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final BillMapper billMapper;
    private final BillNumberGenerator billNumberGenerator;
    private final BillingConfig billingConfig;
    private final BillProducer billProducer;
    private final PaymentGatewayFactory gatewayFactory;


    // ============================================================
    // Current Logged-in User
    // ============================================================

    private JwtUserDetails getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof JwtUserDetails)) {

            throw new IllegalStateException(
                    "Authenticated user not found"
            );
        }

        return (JwtUserDetails) authentication.getPrincipal();
    }

    private Long getCurrentRestaurantId() {
        return getCurrentUser().getRestaurantId();
    }


    // ============================================================
    // Generate Bill
    // ============================================================

    @Override
    @Transactional
    public BillResponse generateBill(OrderCreatedEvent event) {

        if (event == null) {
            throw new IllegalArgumentException("Order event cannot be null");
        }

        Long restaurantId = getCurrentRestaurantId();

        log.info(
                "Generating bill. restaurantId={}, orderId={}, orderNumber={}",
                restaurantId,
                event.getOrderId(),
                event.getOrderNumber()
        );

        // --------------------------------------------------------
        // Idempotency
        // --------------------------------------------------------

        if (billRepository.existsByRestaurantIdAndOrderId(
                restaurantId,
                event.getOrderId()
        )) {

            log.warn(
                    "Bill already exists. restaurantId={}, orderId={}",
                    restaurantId,
                    event.getOrderId()
            );

            return billMapper.toResponse(
                    billRepository
                            .findByRestaurantIdAndOrderId(
                                    restaurantId,
                                    event.getOrderId()
                            )
                            .orElseThrow(() ->
                                    new BillNotFoundException(
                                            "Bill not found for orderId: "
                                                    + event.getOrderId()
                                    )
                            )
            );
        }

        // --------------------------------------------------------
        // Build Bill
        // --------------------------------------------------------

        Bill bill = Bill.builder()
                .billNumber(billNumberGenerator.generate())
                .restaurantId(restaurantId)
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .tableNumber(event.getTableNumber())
                .waiterId(event.getWaiterId())
                .waiterEmail(event.getWaiterEmail())
                .status(BillStatus.GENERATED)
                .build();

        // --------------------------------------------------------
        // Add Items
        // --------------------------------------------------------

        if (event.getItems() != null) {

            event.getItems()
                    .stream()
                    .map(this::toBillItem)
                    .forEach(bill::addItem);
        }

        // --------------------------------------------------------
        // Calculate Amounts
        // --------------------------------------------------------

        BigDecimal subtotal = calculateSubtotal(bill);

        BigDecimal gstPercentage =
                billingConfig.getGstPercentage();

        BigDecimal gstAmount =
                calculatePercentage(
                        subtotal,
                        gstPercentage
                );

        BigDecimal serviceChargePercentage =
                billingConfig.isServiceChargeEnabled()
                        ? billingConfig.getServiceChargePercentage()
                        : BigDecimal.ZERO;

        BigDecimal serviceChargeAmount =
                calculatePercentage(
                        subtotal,
                        serviceChargePercentage
                );

        BigDecimal grandTotal =
                subtotal
                        .add(gstAmount)
                        .add(serviceChargeAmount);

        bill.setSubtotal(subtotal);
        bill.setGstPercentage(gstPercentage);
        bill.setGstAmount(gstAmount);
        bill.setServiceChargePct(serviceChargePercentage);
        bill.setServiceChargeAmt(serviceChargeAmount);
        bill.setGrandTotal(grandTotal);

        // --------------------------------------------------------
        // Save
        // --------------------------------------------------------

        Bill savedBill =
                billRepository.save(bill);

        log.info(
                "Bill generated successfully. restaurantId={}, billId={}, billNumber={}, grandTotal={}",
                restaurantId,
                savedBill.getId(),
                savedBill.getBillNumber(),
                savedBill.getGrandTotal()
        );

        // --------------------------------------------------------
        // Kafka Event
        // --------------------------------------------------------

        billProducer.publishBillGenerated(savedBill);

        return billMapper.toResponse(savedBill);
    }


    // ============================================================
    // Process Payment
    // ============================================================

    @Override
    @Transactional
    public BillResponse processPayment(
            Long billId,
            PaymentRequest request
    ) {

        Long restaurantId = getCurrentRestaurantId();

        log.info(
                "Processing payment. restaurantId={}, billId={}, method={}",
                restaurantId,
                billId,
                request.getPaymentMethod()
        );

        // --------------------------------------------------------
        // Tenant-aware Bill Lookup
        // --------------------------------------------------------

        Bill bill =
                findById(
                        restaurantId,
                        billId
                );

        // --------------------------------------------------------
        // State Validation
        // --------------------------------------------------------

        validatePaymentState(bill);

        // --------------------------------------------------------
        // Idempotency
        // --------------------------------------------------------

        if (request.getIdempotencyKey() != null) {

            billRepository
                    .findByRestaurantIdAndIdempotencyKey(
                            restaurantId,
                            request.getIdempotencyKey()
                    )
                    .ifPresent(existingBill -> {

                        if (!existingBill.getId().equals(billId)) {

                            throw new DuplicatePaymentException(
                                    request.getIdempotencyKey()
                            );
                        }
                    });

            bill.setIdempotencyKey(
                    request.getIdempotencyKey()
            );
        }

        // --------------------------------------------------------
        // Payment Pending
        // --------------------------------------------------------

        bill.setStatus(
                BillStatus.PAYMENT_PENDING
        );

        bill.setPaymentMethod(
                request.getPaymentMethod()
        );

        // --------------------------------------------------------
        // Gateway
        // --------------------------------------------------------

        PaymentGateway gateway =
                gatewayFactory.getActiveGateway();

        // Never trust amount from request
        request.setAmount(
                bill.getGrandTotal()
        );

        request.setBillId(
                bill.getId()
        );

        request.setBillNumber(
                bill.getBillNumber()
        );

        // --------------------------------------------------------
        // Transaction
        // --------------------------------------------------------

        String transactionReference =
                generateTransactionReference(
                        bill.getId()
                );

        PaymentTransaction transaction =
                PaymentTransaction.builder()
                        .transactionRef(transactionReference)
                        .amount(bill.getGrandTotal())
                        .paymentMethod(
                                request.getPaymentMethod()
                        )
                        .gateway(
                                gateway.getGatewayName()
                        )
                        .status(
                                TransactionStatus.INITIATED
                        )
                        .build();

        bill.addTransaction(transaction);

        // --------------------------------------------------------
        // Call Gateway
        // --------------------------------------------------------

        PaymentGatewayResponse response =
                gateway.processPayment(request);

        // --------------------------------------------------------
        // Success
        // --------------------------------------------------------

        if (response.isSuccess()) {

            transaction.setStatus(
                    TransactionStatus.SUCCESS
            );

            transaction.setGatewayTransactionId(
                    response.getGatewayTransactionId()
            );

            transaction.setGatewayResponse(
                    response.getRawResponse()
            );

            /*
             * Payment URL intentionally not stored.
             *
             * Actual PAID status should normally be handled
             * after gateway webhook/payment confirmation.
             */

            bill.setStatus(
                    BillStatus.PAYMENT_PENDING
            );

            Bill savedBill =
                    billRepository.save(bill);

            billProducer.publishPaymentCompleted(
                    savedBill,
                    transaction
            );

            log.info(
                    "Payment initiated successfully. restaurantId={}, billId={}",
                    restaurantId,
                    billId
            );

            return billMapper.toResponse(
                    savedBill
            );
        }

        // --------------------------------------------------------
        // Failure
        // --------------------------------------------------------

        transaction.setStatus(
                TransactionStatus.FAILED
        );

        transaction.setFailureReason(
                response.getFailureReason()
        );

        transaction.setGatewayResponse(
                response.getRawResponse()
        );

        bill.setStatus(
                BillStatus.FAILED
        );

        Bill savedBill =
                billRepository.save(bill);

        billProducer.publishPaymentFailed(
                savedBill,
                transaction
        );

        throw new PaymentFailedException(
                "Payment failed via "
                        + gateway.getGatewayName()
                        + ": "
                        + response.getFailureReason()
        );
    }


    // ============================================================
    // Read Operations
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillById(Long billId) {

        Long restaurantId =
                getCurrentRestaurantId();

        return billMapper.toResponse(
                findById(
                        restaurantId,
                        billId
                )
        );
    }


    @Override
    @Transactional(readOnly = true)
    public BillResponse getBillByOrderId(Long orderId) {

        Long restaurantId =
                getCurrentRestaurantId();

        return billMapper.toResponse(
                billRepository
                        .findByRestaurantIdAndOrderId(
                                restaurantId,
                                orderId
                        )
                        .orElseThrow(() ->
                                new BillNotFoundException(
                                        "Bill not found for orderId: "
                                                + orderId
                                )
                        )
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getAllBills() {

        Long restaurantId =
                getCurrentRestaurantId();

        // IMPORTANT:
        // Never use findAll() in multi-tenant service.

        return billMapper.toResponseList(
                billRepository.findByRestaurantId(
                        restaurantId
                )
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getBillsByStatus(
            BillStatus status
    ) {

        Long restaurantId =
                getCurrentRestaurantId();

        return billMapper.toResponseList(
                billRepository.findByRestaurantIdAndStatus(
                        restaurantId,
                        status
                )
        );
    }


    @Override
    @Transactional(readOnly = true)
    public List<BillResponse> getBillsByWaiterId(
            Long waiterId
    ) {

        Long restaurantId =
                getCurrentRestaurantId();

        return billMapper.toResponseList(
                billRepository.findByRestaurantIdAndWaiterId(
                        restaurantId,
                        waiterId
                )
        );
    }


    // ============================================================
    // Kafka Batch Processing
    // ============================================================

    @Override
    @Transactional
    public void processOrderCreatedEvents(
            List<OrderCreatedEvent> events
    ) {

        if (events == null || events.isEmpty()) {
            return;
        }

        log.info(
                "Processing {} order-created events",
                events.size()
        );


        Map<Long, List<OrderCreatedEvent>> eventsByRestaurant =
                events.stream()
                        .filter(event -> event.getRestaurantId() != null)
                        .collect(
                                Collectors.groupingBy(
                                        OrderCreatedEvent::getRestaurantId
                                )
                        );

        Set<String> existingKeys =
                new HashSet<>();

        // --------------------------------------------------------
        // Find Existing Bills
        // --------------------------------------------------------

        for (Map.Entry<Long, List<OrderCreatedEvent>> entry :
                eventsByRestaurant.entrySet()) {

            Long restaurantId =
                    entry.getKey();

            List<Long> orderIds =
                    entry.getValue()
                            .stream()
                            .map(OrderCreatedEvent::getOrderId)
                            .filter(id -> id != null)
                            .distinct()
                            .toList();

            if (orderIds.isEmpty()) {
                continue;
            }

            billRepository
                    .findByRestaurantIdAndOrderIdIn(
                            restaurantId,
                            orderIds
                    )
                    .forEach(bill ->
                            existingKeys.add(
                                    createTenantKey(
                                            bill.getRestaurantId(),
                                            bill.getOrderId()
                                    )
                            )
                    );
        }

        // --------------------------------------------------------
        // Create Only New Bills
        // --------------------------------------------------------

        List<Bill> newBills =
                events.stream()
                        .filter(event ->
                                event.getRestaurantId() != null
                                        && event.getOrderId() != null
                        )
                        .filter(event ->
                                !existingKeys.contains(
                                        createTenantKey(
                                                event.getRestaurantId(),
                                                event.getOrderId()
                                        )
                                )
                        )
                        .map(this::toBill)
                        .toList();

        if (newBills.isEmpty()) {

            log.info(
                    "All {} Kafka events were duplicates",
                    events.size()
            );

            return;
        }

        // --------------------------------------------------------
        // Batch Insert
        // --------------------------------------------------------

        billRepository.saveAll(newBills);

        log.info(
                "Inserted {} new bills from {} Kafka events",
                newBills.size(),
                events.size()
        );
    }


    // ============================================================
    // Private Helpers
    // ============================================================

    private Bill findById(
            Long restaurantId,
            Long billId
    ) {

        return billRepository
                .findByRestaurantIdAndId(
                        restaurantId,
                        billId
                )
                .orElseThrow(() ->
                        new BillNotFoundException(
                                billId
                        )
                );
    }


    private void validatePaymentState(
            Bill bill
    ) {

        if (bill.getStatus() == BillStatus.PAID) {

            throw new InvalidBillStateException(
                    bill.getStatus(),
                    "payment"
            );
        }

        if (bill.getStatus() == BillStatus.CANCELLED
                || bill.getStatus() == BillStatus.REFUNDED) {

            throw new InvalidBillStateException(
                    bill.getStatus(),
                    "payment"
            );
        }

        if (bill.getStatus() == BillStatus.PENDING) {

            throw new InvalidBillStateException(
                    bill.getStatus(),
                    "payment - bill generation not complete"
            );
        }
    }


    private BillItem toBillItem(
            OrderCreatedEvent.OrderItemEvent item
    ) {

        return BillItem.builder()
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .pricePerUnit(item.getPricePerUnit())
                .subtotal(item.getSubtotal())
                .build();
    }


    private Bill toBill(
            OrderCreatedEvent event
    ) {

        Bill bill =
                Bill.builder()
                        .restaurantId(
                                event.getRestaurantId()
                        )
                        .billNumber(
                                billNumberGenerator.generate()
                        )
                        .orderId(
                                event.getOrderId()
                        )
                        .orderNumber(
                                event.getOrderNumber()
                        )
                        .tableNumber(
                                event.getTableNumber()
                        )
                        .waiterId(
                                event.getWaiterId()
                        )
                        .waiterEmail(
                                event.getWaiterEmail()
                        )
                        .status(
                                BillStatus.GENERATED
                        )
                        .build();

        /*
         * Kafka event already contains item-level pricing,
         * so create BillItems here as well.
         */

        if (event.getItems() != null) {

            event.getItems()
                    .stream()
                    .map(this::toBillItem)
                    .forEach(bill::addItem);
        }

        // --------------------------------------------------------
        // Calculate bill amounts
        // --------------------------------------------------------

        BigDecimal subtotal =
                calculateSubtotal(bill);

        BigDecimal gstPercentage =
                billingConfig.getGstPercentage();

        BigDecimal gstAmount =
                calculatePercentage(
                        subtotal,
                        gstPercentage
                );

        BigDecimal serviceChargePercentage =
                billingConfig.isServiceChargeEnabled()
                        ? billingConfig.getServiceChargePercentage()
                        : BigDecimal.ZERO;

        BigDecimal serviceChargeAmount =
                calculatePercentage(
                        subtotal,
                        serviceChargePercentage
                );

        BigDecimal grandTotal =
                subtotal
                        .add(gstAmount)
                        .add(serviceChargeAmount);

        bill.setSubtotal(subtotal);
        bill.setGstPercentage(gstPercentage);
        bill.setGstAmount(gstAmount);
        bill.setServiceChargePct(
                serviceChargePercentage
        );
        bill.setServiceChargeAmt(
                serviceChargeAmount
        );
        bill.setGrandTotal(
                grandTotal
        );

        return bill;
    }


    private BigDecimal calculateSubtotal(
            Bill bill
    ) {

        return bill.getItems()
                .stream()
                .map(BillItem::getSubtotal)
                .filter(amount -> amount != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(
                        2,
                        RoundingMode.HALF_UP
                );
    }


    private BigDecimal calculatePercentage(
            BigDecimal base,
            BigDecimal percentage
    ) {

        if (base == null
                || percentage == null
                || percentage.compareTo(BigDecimal.ZERO) == 0) {

            return BigDecimal.ZERO;
        }

        return base
                .multiply(percentage)
                .divide(
                        BigDecimal.valueOf(100),
                        2,
                        RoundingMode.HALF_UP
                );
    }


    private String createTenantKey(
            Long restaurantId,
            Long orderId
    ) {

        return restaurantId + ":" + orderId;
    }


    private String generateTransactionReference(
            Long billId
    ) {

        return "TXN-"
                + billId
                + "-"
                + System.currentTimeMillis();
    }
}
