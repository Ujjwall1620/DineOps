package com.restaurant.billservice.service;

import com.restaurant.billservice.dto.response.BillItemResponse;
import com.restaurant.billservice.dto.response.BillResponse;
import com.restaurant.billservice.dto.response.PaymentTransactionResponse;
import com.restaurant.billservice.entity.Bill;
import com.restaurant.billservice.entity.BillItem;
import com.restaurant.billservice.entity.PaymentTransaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BillMapper {

    public BillResponse toResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .orderId(bill.getOrderId())
                .orderNumber(bill.getOrderNumber())
                .tableNumber(bill.getTableNumber())
                .waiterId(bill.getWaiterId())
                .waiterName(bill.getWaiterName())
                .subtotal(bill.getSubtotal())
                .gstPercentage(bill.getGstPercentage())
                .gstAmount(bill.getGstAmount())
                .serviceChargePct(bill.getServiceChargePct())
                .serviceChargeAmt(bill.getServiceChargeAmt())
                .grandTotal(bill.getGrandTotal())
                .status(bill.getStatus())
                .paymentMethod(bill.getPaymentMethod())
                .paymentUrl(bill.getPaymentUrl())
                .items(toItemResponseList(bill.getItems()))
                .transactions(toTransactionResponseList(bill.getTransactions()))
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }

    public List<BillResponse> toResponseList(List<Bill> bills) {
        return bills.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private BillItemResponse toItemResponse(BillItem item) {
        return BillItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .pricePerUnit(item.getPricePerUnit())
                .subtotal(item.getSubtotal())
                .build();
    }

    private List<BillItemResponse> toItemResponseList(List<BillItem> items) {
        return items.stream().map(this::toItemResponse).collect(Collectors.toList());
    }

    private PaymentTransactionResponse toTransactionResponse(PaymentTransaction txn) {
        return PaymentTransactionResponse.builder()
                .id(txn.getId())
                .transactionRef(txn.getTransactionRef())
                .amount(txn.getAmount())
                .paymentMethod(txn.getPaymentMethod())
                .gateway(txn.getGateway())
                .gatewayTransactionId(txn.getGatewayTransactionId())
                .status(txn.getStatus())
                .failureReason(txn.getFailureReason())
                .createdAt(txn.getCreatedAt())
                .build();
    }

    private List<PaymentTransactionResponse> toTransactionResponseList(
            List<PaymentTransaction> transactions) {
        return transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }
}
