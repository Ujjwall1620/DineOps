package com.restaurant.billservice.payment.gateway;

import com.restaurant.billservice.dto.request.PaymentRequest;
import com.restaurant.billservice.dto.response.PaymentGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Dummy gateway for development and testing.
 * Always succeeds. No external API calls.
 * Set payment.gateway=dummy in application.properties.
 */
@Component
@Slf4j
public class DummyPaymentGateway implements PaymentGateway {

    @Override
    public PaymentGatewayResponse processPayment(PaymentRequest request) {
        String gatewayTxnId = "DUMMY-" + UUID.randomUUID().toString().toUpperCase();

        log.info("[DummyGateway] Processing payment of ₹{} via {} — txnId: {}",
                request.getAmount(), request.getPaymentMethod(), gatewayTxnId);

        String rawResponse = String.format(
                "{\"gateway\":\"dummy\",\"txnId\":\"%s\",\"amount\":%s,\"status\":\"SUCCESS\"}",
                gatewayTxnId, request.getAmount());

        return PaymentGatewayResponse.builder()
                .success(true)
                .gatewayTransactionId(gatewayTxnId)
                .rawResponse(rawResponse)
                .amountProcessed(request.getAmount())
                .gatewayName(getGatewayName())
                .build();
    }

    @Override
    public PaymentGatewayResponse processRefund(String gatewayTransactionId,
                                                 BigDecimal amount) {
        String refundId = "DUMMY-REFUND-" + UUID.randomUUID().toString().toUpperCase();

        log.info("[DummyGateway] Processing refund of ₹{} for txnId: {}",
                amount, gatewayTransactionId);

        String rawResponse = String.format(
                "{\"gateway\":\"dummy\",\"refundId\":\"%s\",\"originalTxn\":\"%s\",\"status\":\"REFUNDED\"}",
                refundId, gatewayTransactionId);

        return PaymentGatewayResponse.builder()
                .success(true)
                .gatewayTransactionId(refundId)
                .rawResponse(rawResponse)
                .amountProcessed(amount)
                .gatewayName(getGatewayName())
                .build();
    }

    @Override
    public String getGatewayName() {
        return "dummy";
    }
}
