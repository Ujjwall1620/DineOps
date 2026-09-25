package com.example.restaurant.kafka.consumer;

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

import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCancelledConsumer {

    private final KitchenTicketRepository ticketRepository;

    @KafkaListener(
            topics = "${kafka.topic.order-cancelled}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderCancelledListenerContainerFactory"
    )
    @Transactional
    public void consumeOrderCancelled(@Payload OrderCancelledEvent event, Acknowledgment acknowledgment) {

        log.info("Received order-cancelled event for restaurantId: {}, orderId: {}",
                event.getRestaurantId(), event.getOrderId());

        try {
            KitchenTicket ticket = ticketRepository
                    .findByRestaurantIdAndOrderId(event.getRestaurantId(), event.getOrderId())
                    .orElse(null);

            if (ticket == null) {
                log.warn("No kitchen ticket found for restaurantId: {}, orderId: {} on cancel. Will retry.",
                        event.getRestaurantId(), event.getOrderId());
                throw new IllegalStateException(
                        "Kitchen ticket not found yet for orderId: " + event.getOrderId());
            }


            if (ticket.getStatus() == KitchenStatus.CANCELLED) {
                log.info("Ticket {} already cancelled. Skipping.", ticket.getId());
                acknowledgment.acknowledge();
                return;
            }

            if (ticket.getStatus() == KitchenStatus.READY
                    || ticket.getStatus() == KitchenStatus.COMPLETED) {
                log.warn("Cannot cancel ticket {} — already in {} state. " +
                                "Order service ko is mismatch ke baare me batana chahiye.",
                        ticket.getId(), ticket.getStatus());
                acknowledgment.acknowledge();
                return;
            }

            ticket.setStatus(KitchenStatus.CANCELLED);
            ticket.setCancelledAt(LocalDateTime.from(Instant.now()));
            ticketRepository.save(ticket);

            log.info("Kitchen ticket {} cancelled for orderId: {}", ticket.getId(), event.getOrderId());
            acknowledgment.acknowledge();

        } catch (Exception ex) {
            log.error("Failed to cancel kitchen ticket for orderId: {}. Error: {}",
                    event.getOrderId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}