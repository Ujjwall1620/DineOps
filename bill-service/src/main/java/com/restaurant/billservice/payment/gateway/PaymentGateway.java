package com.restaurant.billservice.payment.gateway;

import com.restaurant.billservice.dto.request.PaymentRequest;
import com.restaurant.billservice.dto.response.PaymentGatewayResponse;

/**
 * Strategy interface for payment gateways.
 *
 * Each implementation handles a specific provider (Razorpay, Stripe, Dummy).
 * The active implementation is selected at runtime by {@link PaymentGatewayFactory}
 * based on {@code payment.gateway} in application.properties.
 *
 * To add a new gateway:
 * 1. Create a new class implementing this interface.
 * 2. Annotate it with @Component.
 * 3. Override getGatewayName() to return the config key.
 * 4. Set payment.gateway=your_key in application.properties.
 * No other changes needed — factory picks it up automatically.
 */
public interface PaymentGateway {

    /**
     * Process a payment request.
     * @param request Payment details including amount and method.
     * @return Gateway response with transaction ID and status.
     */
    PaymentGatewayResponse processPayment(PaymentRequest request);

    /**
     * Process a refund for an existing transaction.
     * @param gatewayTransactionId The gateway's own transaction ID.
     * @param amount               Amount to refund.
     * @return Gateway response with refund details.
     */
    PaymentGatewayResponse processRefund(String gatewayTransactionId, java.math.BigDecimal amount);

    /**
     * Unique identifier matching the value in application.properties.
     * Example: "razorpay", "stripe", "dummy"
     */
    String getGatewayName();
}
