package com.restaurant.billservice.kafka.consumer;

import com.restaurant.billservice.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderReadyConsumer {

    private final BillService billService;

    /**
     * Consumes the enriched {@code order-ready} event published by Order Service.
     *
     * <p>This event carries frozen item prices, so Bill Service can calculate
     * tax and totals without calling any other service.
     *
     * <p>Idempotency: Bill Service's generateBill() checks existsByOrderId()
     * before inserting — same pattern as Kitchen Service's consumer.
     *
     * <p>Manual acknowledgment: offset committed only after bill is successfully
     * persisted — identical to Kitchen Service's OrderCreatedConsumer.
     */
    @KafkaListener(
            topics           = "${kafka.topic.order-ready}",
            groupId          = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeOrderReady(@Payload OrderReadyEvent event,
                                  Acknowledgment acknowledgment) {

        log.info("Received order-ready event for orderId: {}, orderNumber: {}",
                event.getOrderId(), event.getOrderNumber());

        try {
            billService.generateBill(event);

            // Commit offset only after successful bill generation
            acknowledgment.acknowledge();

            log.info("Bill generated and offset committed for orderId: {}",
                    event.getOrderId());

        } catch (Exception ex) {
            log.error("Failed to generate bill for orderId: {}. Error: {}",
                    event.getOrderId(), ex.getMessage(), ex);
            // Do NOT acknowledge — Kafka will redeliver
            // Mirrors Kitchen Service's consumer error-handling pattern exactly
            throw ex;
        }
    }
}
