package com.restaurant.orderservice.kafka;

import com.restaurant.orderservice.entity.Order;
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
public class OrderProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${kafka.topic.order-created}")
    private String orderCreatedTopic;

    @Value("${kafka.topic.order-updated}")
    private String orderUpdatedTopic;

    @Value("${kafka.topic.order-cancelled}")
    private String orderCancelledTopic;

    public void publishOrderCreated(Order order) {
        OrderEvent event = buildOrderEvent(order, "ORDER_CREATED");
        sendEvent(orderCreatedTopic, event);
        log.info("Published ORDER_CREATED event for order: {}", order.getOrderNumber());
    }

    public void publishOrderUpdated(Order order) {
        OrderEvent event = buildOrderEvent(order, "ORDER_UPDATED");
        sendEvent(orderUpdatedTopic, event);
        log.info("Published ORDER_UPDATED event for order: {}", order.getOrderNumber());
    }

    public void publishOrderCancelled(Order order) {
        OrderEvent event = buildOrderEvent(order, "ORDER_CANCELLED");
        sendEvent(orderCancelledTopic, event);
        log.info("Published ORDER_CANCELLED event for order: {}", order.getOrderNumber());
    }

    private void sendEvent(String topic, OrderEvent event) {
        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(topic, event.getOrderId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event [{}] to topic [{}]: {}",
                        event.getEventType(), topic, ex.getMessage(), ex);
            } else {
                log.debug("Event [{}] published to topic [{}], partition [{}], offset [{}]",
                        event.getEventType(), topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private OrderEvent buildOrderEvent(Order order, String eventType) {
        return OrderEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableNumber(order.getTableNumber())
                .waiterId(order.getWaiterId())
                .waiterEmail(order.getWaiterEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .eventType(eventType)
                .eventTimestamp(LocalDateTime.now())
                .build();
    }
}
