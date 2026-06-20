package com.restaurant.kitchenservice.kafka.producer;

import com.restaurant.kitchenservice.entity.KitchenTicket;
import com.restaurant.kitchenservice.exception.KafkaPublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class KitchenProducer {

    private final KafkaTemplate<String, KitchenStatusEvent> kafkaTemplate;

    @Value("${kafka.topic.order-cooking-started}")
    private String cookingStartedTopic;

    @Value("${kafka.topic.order-ready}")
    private String orderReadyTopic;

    // ─── Public API ───────────────────────────────────────────────────────────

    public void publishCookingStarted(KitchenTicket ticket) {
        KitchenStatusEvent event = buildEvent(ticket, "IN_PREPARATION", "ORDER_COOKING_STARTED");
        send(cookingStartedTopic, event);
        log.info("Published ORDER_COOKING_STARTED for orderId: {}", ticket.getOrderId());
    }

    public void publishOrderReady(KitchenTicket ticket) {
        KitchenStatusEvent event = buildEvent(ticket, "READY", "ORDER_READY");
        send(orderReadyTopic, event);
        log.info("Published ORDER_READY for orderId: {}", ticket.getOrderId());
    }

    // ─── Internal helpers ─────────────────────────────────────────────────────

    private void send(String topic, KitchenStatusEvent event) {
        CompletableFuture<SendResult<String, KitchenStatusEvent>> future =
                kafkaTemplate.send(topic, event.getOrderId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event [{}] to topic [{}] for orderId [{}]: {}",
                        event.getEventType(), topic, event.getOrderId(), ex.getMessage(), ex);
                // Re-throw wrapped so caller can catch if needed
                throw new KafkaPublishException(
                        "Failed to publish Kafka event [" + event.getEventType() + "] "
                        + "to topic [" + topic + "]: " + ex.getMessage());
            } else {
                log.debug("Event [{}] sent to topic [{}], partition [{}], offset [{}]",
                        event.getEventType(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private KitchenStatusEvent buildEvent(KitchenTicket ticket,
                                          String status,
                                          String eventType) {
        return KitchenStatusEvent.builder()
                .orderId(ticket.getOrderId())
                .orderNumber(ticket.getOrderNumber())
                .tableNumber(ticket.getTableNumber())
                .status(status)
                .chefId(ticket.getChefId())
                .chefName(ticket.getChefName())
                .eventType(eventType)
                .eventTimestamp(LocalDateTime.now())
                .build();
    }
}
