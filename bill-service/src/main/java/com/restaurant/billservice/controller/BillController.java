package com.restaurant.billservice.controller;

import com.restaurant.billservice.dto.request.PaymentRequest;
import com.restaurant.billservice.dto.response.ApiResponse;
import com.restaurant.billservice.dto.response.BillResponse;
import com.restaurant.billservice.entity.Bill;
import com.restaurant.billservice.entity.PaymentTransaction;
import com.restaurant.billservice.enums.BillStatus;
import com.restaurant.billservice.enums.PaymentMethod;
import com.restaurant.billservice.enums.TransactionStatus;
import com.restaurant.billservice.exception.BillNotFoundException;
import com.restaurant.billservice.exception.InvalidBillStateException;
import com.restaurant.billservice.kafka.producer.BillProducer;
import com.restaurant.billservice.payment.gateway.RazorpayPaymentGateway;
import com.restaurant.billservice.repository.BillRepository;
import com.restaurant.billservice.service.BillService;
import com.restaurant.billservice.enums.BillStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Bill Service", description = "Bill generation and payment processing APIs")
public class BillController {

    private final BillService              billService;
    private final RazorpayPaymentGateway   razorpayGateway;
    private final BillRepository           billRepository;
    private final BillProducer             billProducer;

    // ─────────────────────────────────────────────────────────────────────────
    // READ OPERATIONS
    // ─────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Get all bills")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BillResponse>>> getAllBills() {
        log.debug("GET /api/bills");
        return ResponseEntity.ok(ApiResponse.success("Bills retrieved",
                billService.getAllBills()));
    }

    @Operation(summary = "Get bill by ID")
    @GetMapping("/{billId}")
    public ResponseEntity<ApiResponse<BillResponse>> getBillById(@PathVariable Long billId) {
        log.debug("GET /api/bills/{}", billId);
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved",
                billService.getBillById(billId)));
    }

    @Operation(summary = "Get bill by Order ID")
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<BillResponse>> getBillByOrderId(
            @PathVariable Long orderId) {
        log.debug("GET /api/bills/order/{}", orderId);
        return ResponseEntity.ok(ApiResponse.success("Bill retrieved for order",
                billService.getBillByOrderId(orderId)));
    }

    @Operation(summary = "Get bills by status")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getBillsByStatus(
            @PathVariable BillStatus status) {
        log.debug("GET /api/bills/status/{}", status);
        return ResponseEntity.ok(ApiResponse.success("Bills by status retrieved",
                billService.getBillsByStatus(status)));
    }

    @Operation(summary = "Get bills by waiter ID")
    @GetMapping("/waiter/{waiterId}")
    public ResponseEntity<ApiResponse<List<BillResponse>>> getBillsByWaiter(
            @PathVariable Long waiterId) {
        log.debug("GET /api/bills/waiter/{}", waiterId);
        return ResponseEntity.ok(ApiResponse.success("Bills by waiter retrieved",
                billService.getBillsByWaiterId(waiterId)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PAYMENT LINK GENERATION
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/bills/{billId}/payment-link
     *
     * Generates a Razorpay Payment Link and returns the short URL.
     *
     * FLOW:
     * 1. Cashier calls this endpoint
     * 2. You get back: { "paymentUrl": "https://rzp.io/i/abc123" }
     * 3. Send that URL to the customer via WhatsApp/SMS
     * 4. Customer opens the link and pays
     * 5. Razorpay calls your /webhook endpoint automatically
     * 6. Bill is marked PAID
     *
     * No frontend needed. No checkout popup. Customer pays on Razorpay's page.
     */
    @Operation(
        summary = "Generate Razorpay payment link",
        description = "Creates a payment link. Send the returned paymentUrl to the customer " +
                      "via WhatsApp or SMS. No frontend needed.")
    @PostMapping("/{billId}/payment-link")
    public ResponseEntity<ApiResponse<BillResponse>> generatePaymentLink(
            @Parameter(description = "Bill ID to generate payment link for")
            @PathVariable Long billId) {

        log.info("POST /api/bills/{}/payment-link", billId);

        PaymentRequest request = PaymentRequest.builder()
                .paymentMethod(PaymentMethod.UPI) // Razorpay link supports all methods
                .build();

        return ResponseEntity.ok(ApiResponse.success(
                "Payment link generated. Send paymentUrl to customer.",
                billService.processPayment(billId, request)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RAZORPAY WEBHOOK
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/bills/webhook/razorpay
     *
     * Razorpay calls this URL automatically after a customer pays.
     * You configure this URL in your Razorpay Dashboard.
     *
     * HOW TO SET UP IN RAZORPAY DASHBOARD:
     * 1. Go to dashboard.razorpay.com
     * 2. Settings → Webhooks → Add New Webhook
     * 3. URL: https://your-domain.com/api/bills/webhook/razorpay
     *    (For local testing, use ngrok — see instructions below)
     * 4. Secret: set any secret string, put same in payment.razorpay.webhook-secret
     * 5. Events to subscribe: payment_link.paid
     *
     * FOR LOCAL TESTING WITH NGROK:
     * 1. Install ngrok: https://ngrok.com
     * 2. Run: ngrok http 8084
     * 3. Copy the https URL, e.g. https://abc123.ngrok.io
     * 4. Set webhook URL to: https://abc123.ngrok.io/api/bills/webhook/razorpay
     * 5. Every time you restart ngrok, update the URL in Razorpay dashboard
     *
     * IMPORTANT: This endpoint must NOT require authentication.
     * Razorpay cannot send a JWT token. Security is handled by signature verification.
     */
    @Operation(
        summary = "Razorpay webhook receiver",
        description = "Called automatically by Razorpay after payment. " +
                      "Configure this URL in Razorpay Dashboard → Settings → Webhooks.")
    @PostMapping("/webhook/razorpay")
    @Transactional
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestHeader("X-Razorpay-Signature") String razorpaySignature,
            @RequestBody String rawBody) {

        log.info("Webhook received from Razorpay");

        // ── Step 1: Verify the webhook is genuinely from Razorpay ────────────────
        // NEVER skip this — without it anyone can fake a payment success
        boolean isValid = razorpayGateway.verifyWebhookSignature(rawBody, razorpaySignature);

        if (!isValid) {
            log.error("INVALID webhook signature — possible fake request. Rejecting.");
            // Return 200 anyway — Razorpay stops retrying on non-2xx
            // but log it as a security alert
            return ResponseEntity.ok("rejected");
        }

        // ── Step 2: Parse webhook payload ────────────────────────────────────────
        try {
            JSONObject payload    = new JSONObject(rawBody);
            String     event      = payload.getString("event");

            log.info("Razorpay webhook event: {}", event);

            // ── Step 3: Handle payment_link.paid event ───────────────────────────
            if ("payment_link.paid".equals(event)) {

                JSONObject paymentLinkEntity = payload
                        .getJSONObject("payload")
                        .getJSONObject("payment_link")
                        .getJSONObject("entity");

                JSONObject paymentEntity = payload
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

                String referenceId     = paymentLinkEntity.getString("reference_id"); // your billNumber
                String razorpayPayId   = paymentEntity.getString("id");              // pay_XXXXX
                int    amountPaid      = paymentEntity.getInt("amount");             // in paise
                String method          = paymentEntity.getString("method");          // card/upi/netbanking

                log.info("Payment received — referenceId: {}, paymentId: {}, amount: ₹{}, method: {}",
                        referenceId, razorpayPayId, amountPaid / 100, method);

                // ── Step 4: Find bill by billNumber (set as reference_id) ────────
                Bill bill = billRepository.findByBillNumber(referenceId)
                        .orElseGet(() -> {
                            log.error("No bill found for referenceId: {}", referenceId);
                            return null;
                        });

                if (bill == null) {
                    return ResponseEntity.ok("bill_not_found");
                }

                // ── Step 5: Idempotency — skip if already paid ───────────────────
                if (bill.getStatus() == BillStatus.PAID) {
                    log.warn("Bill {} already PAID — skipping duplicate webhook", referenceId);
                    return ResponseEntity.ok("already_paid");
                }

                // ── Step 6: Mark bill as PAID ────────────────────────────────────
                PaymentMethod paymentMethod = mapRazorpayMethod(method);

                PaymentTransaction transaction = PaymentTransaction.builder()
                        .transactionRef("WEBHOOK-" + razorpayPayId)
                        .amount(BigDecimal.valueOf(amountPaid).divide(BigDecimal.valueOf(100)))
                        .paymentMethod(paymentMethod)
                        .gateway("razorpay")
                        .gatewayTransactionId(razorpayPayId)
                        .gatewayResponse(rawBody)
                        .status(TransactionStatus.SUCCESS)
                        .build();

                bill.setStatus(BillStatus.PAID);
                bill.setPaymentMethod(paymentMethod);
                bill.addTransaction(transaction);
                billRepository.save(bill);

                log.info("Bill {} marked PAID via webhook. PaymentId: {}",
                        referenceId, razorpayPayId);

                // ── Step 7: Publish Kafka event → Order Service marks order SERVED
                billProducer.publishPaymentCompleted(bill, transaction);
            }

            // Razorpay expects a 200 OK response — always return 200
            return ResponseEntity.ok("ok");

        } catch (Exception ex) {
            log.error("Error processing Razorpay webhook: {}", ex.getMessage(), ex);
            // Still return 200 — non-200 causes Razorpay to retry indefinitely
            return ResponseEntity.ok("error");
        }
    }

    /**
     * GET /api/bills/payment-callback
     *
     * Optional — where Razorpay redirects the browser after payment on web.
     * Mobile UPI apps don't use this.
     * You can show a "Payment successful" page here.
     */
    @GetMapping("/payment-callback")
    public ResponseEntity<ApiResponse<String>> paymentCallback(
            @RequestParam(required = false) String razorpay_payment_link_id,
            @RequestParam(required = false) String razorpay_payment_id,
            @RequestParam(required = false) String razorpay_payment_link_status) {

        log.info("Payment callback received — linkId: {}, paymentId: {}, status: {}",
                razorpay_payment_link_id, razorpay_payment_id,
                razorpay_payment_link_status);

        // Webhook has already marked the bill PAID at this point
        // This is just for browser redirect — show a thank you page
        return ResponseEntity.ok(ApiResponse.success(
                "Payment " + razorpay_payment_link_status + ". Thank you!"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPER
    // ─────────────────────────────────────────────────────────────────────────

    private PaymentMethod mapRazorpayMethod(String method) {
        return switch (method.toLowerCase()) {
            case "card"        -> PaymentMethod.CARD;
            case "upi"         -> PaymentMethod.UPI;
            case "netbanking"  -> PaymentMethod.NET_BANKING;
            case "wallet"      -> PaymentMethod.WALLET;
            default            -> PaymentMethod.CARD;
        };
    }
}
