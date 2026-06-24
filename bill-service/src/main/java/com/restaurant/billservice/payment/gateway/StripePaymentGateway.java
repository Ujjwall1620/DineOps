package com.restaurant.billservice.payment.gateway;

import com.restaurant.billservice.dto.request.PaymentRequest;
import com.restaurant.billservice.dto.response.PaymentGatewayResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stripe gateway implementation.
 * Set payment.gateway=stripe in application.properties.
 *
 * TO ACTIVATE:
 * 1. Add Stripe Java SDK to pom.xml:
 *    <dependency>
 *      <groupId>com.stripe</groupId>
 *      <artifactId>stripe-java</artifactId>
 *      <version>25.1.0</version>
 *    </dependency>
 * 2. Replace stub logic with real Stripe.apiKey + PaymentIntent calls.
 */
@Component
@Slf4j
public class StripePaymentGateway implements PaymentGateway {

    @Value("${payment.stripe.secret-key:NOT_CONFIGURED}")
    private String secretKey;

    @Override
    public PaymentGatewayResponse processPayment(PaymentRequest request) {
        log.info("[StripeGateway] Initiating payment of ₹{} via {}",
                request.getAmount(), request.getPaymentMethod());
        try {
            /*
             * PRODUCTION IMPLEMENTATION:
             * Stripe.apiKey = secretKey;
             * PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
             *     .setAmount(request.getAmount().multiply(BigDecimal.valueOf(100)).longValue())
             *     .setCurrency("inr")
             *     .setConfirm(true)
             *     .setPaymentMethod(request.getStripePaymentMethodId())
             *     .build();
             * PaymentIntent intent = PaymentIntent.create(params);
             * return buildSuccessResponse(intent.getId());
             */

            String gatewayTxnId = "pi_" + UUID.randomUUID().toString().replace("-", "");
            log.info("[StripeGateway] Payment successful, txnId: {}", gatewayTxnId);

            return PaymentGatewayResponse.builder()
                    .success(true)
                    .gatewayTransactionId(gatewayTxnId)
                    .rawResponse("{\"id\":\"" + gatewayTxnId + "\",\"status\":\"succeeded\"}")
                    .amountProcessed(request.getAmount())
                    .gatewayName(getGatewayName())
                    .build();

        } catch (Exception ex) {
            log.error("[StripeGateway] Payment failed: {}", ex.getMessage(), ex);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .failureReason("Stripe payment failed: " + ex.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }

    @Override
    public PaymentGatewayResponse processRefund(String gatewayTransactionId,
                                                 BigDecimal amount) {
        log.info("[StripeGateway] Initiating refund of ₹{} for txnId: {}",
                amount, gatewayTransactionId);
        try {
            String refundId = "re_" + UUID.randomUUID().toString().replace("-", "");
            return PaymentGatewayResponse.builder()
                    .success(true)
                    .gatewayTransactionId(refundId)
                    .rawResponse("{\"id\":\"" + refundId + "\",\"status\":\"succeeded\"}")
                    .amountProcessed(amount)
                    .gatewayName(getGatewayName())
                    .build();
        } catch (Exception ex) {
            log.error("[StripeGateway] Refund failed: {}", ex.getMessage(), ex);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .failureReason("Stripe refund failed: " + ex.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }

    @Override
    public String getGatewayName() {
        return "stripe";
    }
}
