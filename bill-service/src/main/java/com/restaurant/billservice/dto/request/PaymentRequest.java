package com.restaurant.billservice.dto.request;

import com.restaurant.billservice.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
    private BigDecimal amount;
    private String     billNumber;
    private Long       billId;
}
