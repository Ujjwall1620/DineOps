package com.restaurant.billservice.dto.request;

import com.restaurant.billservice.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    /**
     * Client-supplied idempotency key.
     * If same key is used within the configured window, the previous
     * result is returned without re-charging. Prevents double payment on retries.
     */
    private String idempotencyKey;

    // Set internally by service — not from request body
    private BigDecimal amount;
    private String     billNumber;
    private Long       billId;
}
