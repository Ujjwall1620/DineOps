package com.restaurant.billservice.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

/**
 * Centralises all billing configuration values loaded from application.properties.
 * Nothing in the service layer touches @Value directly — always goes through this bean.
 */
@Configuration
@Getter
public class BillingConfig {

    @Value("${billing.tax.gst}")
    private BigDecimal gstPercentage;

    @Value("${billing.service-charge.percentage}")
    private BigDecimal serviceChargePercentage;

    @Value("${billing.service-charge.enabled}")
    private boolean serviceChargeEnabled;

    @Value("${payment.gateway}")
    private String activeGateway;

    @Value("${payment.idempotency.window-minutes:30}")
    private int idempotencyWindowMinutes;
}
