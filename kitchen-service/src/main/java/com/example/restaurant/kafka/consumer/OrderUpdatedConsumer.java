package com.example.restaurant.kafka.consumer;

import com.example.restaurant.entity.KitchenItem;
import com.example.restaurant.entity.KitchenTicket;
import com.example.restaurant.enums.KitchenStatus;
import com.example.restaurant.repository.KitchenTicketRepository;
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
public class OrderUpdatedConsumer {

    private final KitchenTicketRepository ticketRepository;

    @KafkaListener(
            topics = "${kafka.topic.order-updated}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderUpdatedListenerContainerFactory"
    )
    @Transactional
    public void consumeOrderUpdated(@Payload OrderUpdatedEvent event, Acknowledgment acknowledgment) {

        log.info("Received order-updated event for restaurantId: {}, orderId: {}",
                event.getRestaurantId(), event.getOrderId());

        try {
            // ✅ restaurantId + orderId dono se dhundo — multi-tenant safe
            KitchenTicket ticket = ticketRepository
                    .findByRestaurantIdAndOrderId(event.getRestaurantId(), event.getOrderId())
                    .orElse(null);

            if (ticket == null) {
                // CREATED abhi tak process nahi hua ho sakta (ordering guarantee nahi hai
                // alag topics ke beech) — silently skip mat karo, exception throw karo taaki
                // acknowledge na ho aur Kafka thodi der baad retry kare
                log.warn("No kitchen ticket found for restaurantId: {}, orderId: {} on update. " +
                                "Likely order-created not processed yet — will retry.",
                        event.getRestaurantId(), event.getOrderId());
                throw new IllegalStateException(
                        "Kitchen ticket not found yet for orderId: " + event.getOrderId());
            }

            // ✅ Terminal-state guard — cancelled/completed ticket ko dobara mat chhedo
            if (ticket.getStatus() == KitchenStatus.CANCELLED
                    || ticket.getStatus() == KitchenStatus.COMPLETED) {
                log.warn("Ignoring update for orderId: {} — ticket already in terminal state {}",
                        event.getOrderId(), ticket.getStatus());
                acknowledgment.acknowledge();
                return;
            }

            // Poori item list replace karo (diff nahi) — safe aur idempotent
            List<KitchenItem> newItems = event.getItems() == null
                    ? List.of()
                    : event.getItems().stream()
                    .map(payload -> KitchenItem.builder()
                            .menuItemId(payload.getMenuItemId())
                            .menuItemName(payload.getMenuItemName())
                            .quantity(payload.getQuantity())
                            .build())
                    .collect(Collectors.toList());

            ticket.replaceItems(newItems);
            ticketRepository.save(ticket);

            log.info("Kitchen ticket {} updated for orderId: {}", ticket.getId(), event.getOrderId());
            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to update kitchen ticket for orderId: {}. Error: {}",
                    event.getOrderId(), ex.getMessage(), ex);
            throw ex; // acknowledge nahi hua, Kafka redeliver karega
        }
    }
}