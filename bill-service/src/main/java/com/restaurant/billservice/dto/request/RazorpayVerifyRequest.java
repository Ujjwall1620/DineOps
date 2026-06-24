package com.restaurant.billservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The 3 values Razorpay sends to the frontend after customer completes payment.
 * Frontend must POST these to /api/bills/{billId}/verify-payment.
 *
 * NEVER skip verification. Anyone can fake a payment without it.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RazorpayVerifyRequest {

    @NotBlank(message = "razorpay_order_id is required")
    private String razorpayOrderId;     // ord_XXXXXXXXXX — created in step 1

    @NotBlank(message = "razorpay_payment_id is required")
    private String razorpayPaymentId;   // pay_XXXXXXXXXX — created after customer pays

    @NotBlank(message = "razorpay_signature is required")
    private String razorpaySignature;   // HMAC-SHA256 signature for security
}
