package com.restaurant.billservice.kafka.producer;

import com.restaurant.billservice.entity.Bill;
import com.restaurant.billservice.entity.PaymentTransaction;
import com.restaurant.billservice.exception.KafkaPublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class BillProducer {

    private final KafkaTemplate<String, BillGeneratedEvent>  billGeneratedTemplate;
    private final KafkaTemplate<String, PaymentStatusEvent>  paymentStatusTemplate;

    @Value("${kafka.topic.bill-generated}")
    private String billGeneratedTopic;

    @Value("${kafka.topic.payment-completed}")
    private String paymentCompletedTopic;

    @Value("${kafka.topic.payment-failed}")
    private String paymentFailedTopic;

    // ─── Public API ───────────────────────────────────────────────────────────

    public void publishBillGenerated(Bill bill) {
        BillGeneratedEvent event = BillGeneratedEvent.builder()
                .billId(bill.getId())
                .billNumber(bill.getBillNumber())
                .orderId(bill.getOrderId())
                .orderNumber(bill.getOrderNumber())
                .tableNumber(bill.getTableNumber())
                .subtotal(bill.getSubtotal())
                .gstAmount(bill.getGstAmount())
                .serviceChargeAmt(bill.getServiceChargeAmt())
                .grandTotal(bill.getGrandTotal())
                .status(bill.getStatus().name())
                .eventType("BILL_GENERATED")
                .eventTimestamp(LocalDateTime.now())
                .build();

        sendBillEvent(billGeneratedTopic, bill.getOrderId().toString(), event);
        log.info("Published BILL_GENERATED for billId: {}, orderId: {}",
                bill.getId(), bill.getOrderId());
    }

    public void publishPaymentCompleted(Bill bill, PaymentTransaction txn) {
        PaymentStatusEvent event = buildPaymentEvent(bill, txn, "PAYMENT_COMPLETED");
        sendPaymentEvent(paymentCompletedTopic, bill.getOrderId().toString(), event);
        log.info("Published PAYMENT_COMPLETED for billId: {}, txnRef: {}",
                bill.getId(), txn.getTransactionRef());
    }

    public void publishPaymentFailed(Bill bill, PaymentTransaction txn) {
        PaymentStatusEvent event = buildPaymentEvent(bill, txn, "PAYMENT_FAILED");
        sendPaymentEvent(paymentFailedTopic, bill.getOrderId().toString(), event);
        log.info("Published PAYMENT_FAILED for billId: {}, reason: {}",
                bill.getId(), txn.getFailureReason());
    }

    // ─── Internal helpers — mirrors KitchenProducer.send() pattern ────────────

    private void sendBillEvent(String topic, String key, BillGeneratedEvent event) {
        CompletableFuture<SendResult<String, BillGeneratedEvent>> future =
                billGeneratedTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish [{}] to topic [{}]: {}",
                        event.getEventType(), topic, ex.getMessage(), ex);
                throw new KafkaPublishException(
                        "Failed to publish [" + event.getEventType() + "]: " + ex.getMessage());
            } else {
                log.debug("Event [{}] published to [{}], partition [{}], offset [{}]",
                        event.getEventType(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private void sendPaymentEvent(String topic, String key, PaymentStatusEvent event) {
        CompletableFuture<SendResult<String, PaymentStatusEvent>> future =
                paymentStatusTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish [{}] to topic [{}]: {}",
                        event.getEventType(), topic, ex.getMessage(), ex);
                throw new KafkaPublishException(
                        "Failed to publish [" + event.getEventType() + "]: " + ex.getMessage());
            } else {
                log.debug("Event [{}] published to [{}], partition [{}], offset [{}]",
                        event.getEventType(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private PaymentStatusEvent buildPaymentEvent(Bill bill,
                                                  PaymentTransaction txn,
                                                  String eventType) {
        return PaymentStatusEvent.builder()
                .billId(bill.getId())
                .billNumber(bill.getBillNumber())
                .orderId(bill.getOrderId())
                .orderNumber(bill.getOrderNumber())
                .transactionRef(txn.getTransactionRef())
                .amountPaid(txn.getAmount())
                .paymentMethod(txn.getPaymentMethod().name())
                .gateway(txn.getGateway())
                .gatewayTransactionId(txn.getGatewayTransactionId())
                .status(txn.getStatus().name())
                .failureReason(txn.getFailureReason())
                .eventType(eventType)
                .eventTimestamp(LocalDateTime.now())
                .build();
    }
}
