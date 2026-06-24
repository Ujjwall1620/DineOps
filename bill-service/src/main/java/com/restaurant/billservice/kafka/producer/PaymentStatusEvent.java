package com.restaurant.billservice.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusEvent {

    private Long       billId;
    private String     billNumber;
    private Long       orderId;
    private String     orderNumber;
    private String     transactionRef;
    private BigDecimal amountPaid;
    private String     paymentMethod;
    private String     gateway;
    private String     gatewayTransactionId;
    private String     status;          // "SUCCESS" | "FAILED"
    private String     failureReason;   // null on success
    private String     eventType;       // "PAYMENT_COMPLETED" | "PAYMENT_FAILED"
    private LocalDateTime eventTimestamp;
}
