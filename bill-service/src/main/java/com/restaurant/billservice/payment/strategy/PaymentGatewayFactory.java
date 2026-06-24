package com.restaurant.billservice.payment.strategy;

import com.restaurant.billservice.config.BillingConfig;
import com.restaurant.billservice.exception.InvalidPaymentMethodException;
import com.restaurant.billservice.payment.gateway.PaymentGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resolves the active {@link PaymentGateway} from all registered implementations.
 *
 * Spring auto-discovers all @Component implementations of PaymentGateway.
 * The factory maps each by getGatewayName() and resolves the active one
 * from BillingConfig (payment.gateway property).
 *
 * Adding a new gateway requires ZERO changes here — just implement PaymentGateway.
 */
@Component
@Slf4j
public class PaymentGatewayFactory {

    private final Map<String, PaymentGateway> gatewayMap;
    private final BillingConfig billingConfig;

    public PaymentGatewayFactory(List<PaymentGateway> gateways,
                                  BillingConfig billingConfig) {
        this.billingConfig = billingConfig;
        this.gatewayMap = gateways.stream()
                .collect(Collectors.toMap(
                        PaymentGateway::getGatewayName,
                        Function.identity()
                ));
        log.info("Registered payment gateways: {}", this.gatewayMap.keySet());
    }

    /**
     * Returns the active gateway configured in application.properties.
     */
    public PaymentGateway getActiveGateway() {
        String gatewayName = billingConfig.getActiveGateway();
        PaymentGateway gateway = gatewayMap.get(gatewayName);
        if (gateway == null) {
            throw new InvalidPaymentMethodException(
                    "No payment gateway registered for: '" + gatewayName
                    + "'. Available: " + gatewayMap.keySet());
        }
        log.debug("Resolved active payment gateway: {}", gatewayName);
        return gateway;
    }

    /**
     * Returns a specific gateway by name — for admin overrides.
     */
    public PaymentGateway getGateway(String gatewayName) {
        PaymentGateway gateway = gatewayMap.get(gatewayName.toLowerCase());
        if (gateway == null) {
            throw new InvalidPaymentMethodException(
                    "Unknown payment gateway: '" + gatewayName
                    + "'. Available: " + gatewayMap.keySet());
        }
        return gateway;
    }
}
