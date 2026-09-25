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

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final KitchenTicketRepository ticketRepository;

    @KafkaListener(
            topics = "${kafka.topic.order-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    @Transactional
    public void consumeBatch(@Payload List<OrderCreatedEvent> events, Acknowledgment acknowledgment) {

        log.info("Processing batch of {} order-created events", events.size());

        // Step 1: restaurantId ke hisaab se group karo (multi-tenant safe bulk-check ke liye)
        Map<Long, List<OrderCreatedEvent>> eventsByRestaurant = events.stream()
                .collect(Collectors.groupingBy(OrderCreatedEvent::getRestaurantId));

        Set<String> existingKeys = new HashSet<>();
        for (Map.Entry<Long, List<OrderCreatedEvent>> entry : eventsByRestaurant.entrySet()) {
            Long restaurantId = entry.getKey();
            List<Long> orderIds = entry.getValue().stream()
                    .map(OrderCreatedEvent::getOrderId)
                    .toList();

            // Ek hi query — us restaurant ke saare relevant orderIds check ho jaate hain
            ticketRepository.findByRestaurantIdAndOrderIdIn(restaurantId, orderIds)
                    .forEach(t -> existingKeys.add(t.getRestaurantId() + "-" + t.getOrderId()));
        }

        // Step 2: sirf naye (non-duplicate) events ke liye ticket banao
        List<KitchenTicket> newTickets = events.stream()
                .filter(e -> !existingKeys.contains(e.getRestaurantId() + "-" + e.getOrderId()))
                .map(this::toTicket)
                .toList();

        if (newTickets.isEmpty()) {
            log.info("All {} events were duplicates, nothing to insert", events.size());
        } else {
            ticketRepository.saveAll(newTickets); // ek hi batch insert
            log.info("Inserted {} new kitchen tickets", newTickets.size());
        }

        acknowledgment.acknowledge();
    }

    private KitchenTicket toTicket(OrderCreatedEvent event) {
        KitchenTicket ticket = KitchenTicket.builder()
                .restaurantId(event.getRestaurantId())   // ✅ FIX: ye pehle missing tha
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .tableNumber(event.getTableNumber())
                .status(KitchenStatus.PENDING)
                .build();

        if (event.getItems() != null) {
            event.getItems().forEach(payload ->
                    ticket.addItem(KitchenItem.builder()
                            .menuItemId(payload.getMenuItemId())
                            .menuItemName(payload.getMenuItemName())
                            .quantity(payload.getQuantity())
                            .build()));
        }
        return ticket;
    }
}