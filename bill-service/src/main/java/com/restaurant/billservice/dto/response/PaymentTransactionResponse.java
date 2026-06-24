package com.restaurant.billservice.dto.response;

import com.restaurant.billservice.enums.PaymentMethod;
import com.restaurant.billservice.enums.TransactionStatus;
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
public class PaymentTransactionResponse {
    private Long              id;
    private String            transactionRef;
    private BigDecimal        amount;
    private PaymentMethod     paymentMethod;
    private String            gateway;
    private String            gatewayTransactionId;
    private TransactionStatus status;
    private String            failureReason;
    private LocalDateTime     createdAt;
}
