package com.restaurant.kitchenservice.kafka.consumer;

import com.restaurant.kitchenservice.entity.KitchenItem;
import com.restaurant.kitchenservice.entity.KitchenTicket;
import com.restaurant.kitchenservice.enums.KitchenStatus;
import com.restaurant.kitchenservice.repository.KitchenTicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final KitchenTicketRepository ticketRepository;

    /**
     * Consumes {@code order-created} events published by Order Service.
     *
     * <p>Idempotency guard: if a ticket already exists for this orderId
     * (duplicate delivery / retry), the event is silently acknowledged and skipped.
     *
     * <p>Manual acknowledgment (MANUAL_IMMEDIATE) ensures the offset is committed
     * only after the ticket is successfully persisted.
     */
    @KafkaListener(
            topics  = "${kafka.topic.order-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeOrderCreated(@Payload OrderCreatedEvent event,
                                    Acknowledgment acknowledgment) {

        log.info("Received order-created event for orderId: {}, orderNumber: {}",
                event.getOrderId(), event.getOrderNumber());

        // ── Idempotency guard ────────────────────────────────────────────────
        if (ticketRepository.existsByOrderId(event.getOrderId())) {
            log.warn("Duplicate order-created event for orderId: {}. Skipping.", event.getOrderId());
            acknowledgment.acknowledge();
            return;
        }

        try {
            KitchenTicket ticket = KitchenTicket.builder()
                    .orderId(event.getOrderId())
                    .orderNumber(event.getOrderNumber())
                    .tableNumber(event.getTableNumber())
                    .status(KitchenStatus.PENDING)
                    .build();

            // Map order items → kitchen items
            if (event.getItems() != null) {
                List<KitchenItem> kitchenItems = event.getItems().stream()
                        .map(payload -> KitchenItem.builder()
                                .menuItemId(payload.getMenuItemId())
                                .menuItemName(payload.getMenuItemName())
                                .quantity(payload.getQuantity())
                                .build())
                        .collect(Collectors.toList());

                kitchenItems.forEach(ticket::addItem);
            }

            ticketRepository.save(ticket);

            log.info("Kitchen ticket created with id: {} for orderId: {}",
                    ticket.getId(), event.getOrderId());

            // Commit offset only after successful DB write
            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to create kitchen ticket for orderId: {}. Error: {}",
                    event.getOrderId(), ex.getMessage(), ex);
            // Do NOT acknowledge — Kafka will redeliver (up to retry policy)
            throw ex;
        }
    }
}
