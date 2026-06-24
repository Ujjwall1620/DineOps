package com.restaurant.billservice.payment.gateway;

import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.restaurant.billservice.dto.request.PaymentRequest;
import com.restaurant.billservice.dto.response.PaymentGatewayResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Razorpay Payment Links implementation.
 *
 * HOW THIS WORKS:
 * 1. Backend calls Razorpay API → gets a short payment URL
 *    e.g. https://rzp.io/i/abc123
 * 2. You send that URL to the customer (WhatsApp / SMS / email)
 * 3. Customer opens the link on their phone — Razorpay handles everything
 * 4. After payment, Razorpay calls YOUR webhook endpoint
 * 5. Webhook verifies the request and marks the bill as PAID
 *
 * NO frontend needed. NO checkout popup. NO signature verification on your side.
 */
@Component
@Slf4j
public class RazorpayPaymentGateway implements PaymentGateway {

    @Value("${payment.razorpay.key-id}")
    private String keyId;

    @Value("${payment.razorpay.key-secret}")
    private String keySecret;

    @Value("${payment.razorpay.webhook-secret}")
    private String webhookSecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(keyId, keySecret);
            log.info("[RazorpayGateway] Client initialised. KeyId: {}", maskKey(keyId));
        } catch (RazorpayException ex) {
            throw new RuntimeException(
                    "Razorpay client init failed — check your API keys", ex);
        }
    }

    /**
     * Creates a Razorpay Payment Link.
     *
     * Returns a short URL like: https://rzp.io/i/abc123
     * The URL is stored as gatewayTransactionId in PaymentTransaction.
     *
     * Customer opens this link and pays using card / UPI / netbanking.
     * No frontend code needed on your side.
     */
    @Override
    public PaymentGatewayResponse processPayment(PaymentRequest request) {
        log.info("[RazorpayGateway] Creating payment link for bill: {}, amount: ₹{}",
                request.getBillNumber(), request.getAmount());

        try {
            // Razorpay amounts are ALWAYS in paise (₹1 = 100 paise)
            int amountInPaise = request.getAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();

            JSONObject paymentLinkRequest = new JSONObject();
            paymentLinkRequest.put("amount", amountInPaise);
            paymentLinkRequest.put("currency", "INR");

            // This shows on the payment page the customer sees
            paymentLinkRequest.put("description",
                    "Bill " + request.getBillNumber() + " — Table Payment");

            // How long the link stays valid — 30 minutes (in Unix timestamp)
            paymentLinkRequest.put("expire_by",
                    (System.currentTimeMillis() / 1000) + (30 * 60));

            // Your internal reference — use billNumber so you can find it in webhook
            paymentLinkRequest.put("reference_id", request.getBillNumber());

            // Notify customer by SMS/email when link is created (optional)
            // Comment out if you don't want Razorpay to auto-notify
            JSONObject notify = new JSONObject();
            notify.put("sms", false);    // set true to auto-SMS customer
            notify.put("email", false);  // set true to auto-email customer
            paymentLinkRequest.put("notify", notify);

            // Reminder setting — Razorpay will remind customer if unpaid
            paymentLinkRequest.put("reminder_enable", true);

            // Callback URL — where customer lands AFTER payment on web
            // For mobile UPI apps, Razorpay handles redirect automatically
            paymentLinkRequest.put("callback_url",
                    "http://localhost:8084/api/bills/payment-callback");
            paymentLinkRequest.put("callback_method", "get");

            // Store bill metadata — visible in Razorpay dashboard
            JSONObject notes = new JSONObject();
            notes.put("bill_id",     request.getBillId().toString());
            notes.put("bill_number", request.getBillNumber());
            paymentLinkRequest.put("notes", notes);

            PaymentLink paymentLink =
                    razorpayClient.paymentLink.create(paymentLinkRequest);

            String shortUrl   = paymentLink.get("short_url");
            String linkId     = paymentLink.get("id");

            log.info("[RazorpayGateway] Payment link created. linkId: {}, url: {}",
                    linkId, shortUrl);

            return PaymentGatewayResponse.builder()
                    .success(true)
                    .gatewayTransactionId(linkId)   // store linkId for reference
                    .paymentUrl(shortUrl)            // THIS is what you send to customer
                    .rawResponse(paymentLink.toString())
                    .amountProcessed(request.getAmount())
                    .gatewayName(getGatewayName())
                    .build();

        } catch (RazorpayException ex) {
            log.error("[RazorpayGateway] Payment link creation failed: {}",
                    ex.getMessage(), ex);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .failureReason("Payment link creation failed: " + ex.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }

    /**
     * Verifies that the incoming webhook request genuinely came from Razorpay.
     *
     * Razorpay signs every webhook with HMAC-SHA256 using your webhook secret.
     * This method verifies that signature.
     *
     * NEVER process a webhook without calling this first.
     * Without this check, anyone can fake a payment-success webhook.
     *
     * @param rawBody         Raw request body as String (do NOT parse before verifying)
     * @param razorpaySignature  Value of X-Razorpay-Signature header
     */
    public boolean verifyWebhookSignature(String rawBody, String razorpaySignature) {
        try {
            Utils.verifyWebhookSignature(rawBody, razorpaySignature, webhookSecret);
            log.info("[RazorpayGateway] Webhook signature verified successfully");
            return true;
        } catch (RazorpayException ex) {
            log.error("[RazorpayGateway] Webhook signature verification FAILED: {}",
                    ex.getMessage());
            return false;
        }
    }

    /**
     * Refund a payment.
     * @param razorpayPaymentId  The payment ID from webhook (starts with "pay_")
     */
    @Override
    public PaymentGatewayResponse processRefund(String razorpayPaymentId,
                                                 BigDecimal amount) {
        log.info("[RazorpayGateway] Refunding ₹{} for paymentId: {}",
                amount, razorpayPaymentId);
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
            refundRequest.put("speed", "normal");

            com.razorpay.Refund refund =
                    razorpayClient.payments.refund(razorpayPaymentId, refundRequest);

            log.info("[RazorpayGateway] Refund created: {}", (String) refund.get("id"));

            return PaymentGatewayResponse.builder()
                    .success(true)
                    .gatewayTransactionId(refund.get("id"))
                    .rawResponse(refund.toString())
                    .amountProcessed(amount)
                    .gatewayName(getGatewayName())
                    .build();

        } catch (RazorpayException ex) {
            log.error("[RazorpayGateway] Refund failed: {}", ex.getMessage(), ex);
            return PaymentGatewayResponse.builder()
                    .success(false)
                    .failureReason("Refund failed: " + ex.getMessage())
                    .gatewayName(getGatewayName())
                    .build();
        }
    }

    @Override
    public String getGatewayName() {
        return "razorpay";
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 6) return "***";
        return "***" + key.substring(key.length() - 6);
    }
}
