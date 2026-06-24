package com.restaurant.billservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentGatewayResponse {

    private boolean     success;
    private String      gatewayTransactionId;
    private String      paymentUrl;          // Razorpay short URL — send this to customer
    private String      rawResponse;
    private String      failureReason;
    private BigDecimal  amountProcessed;
    private String      gatewayName;
}
