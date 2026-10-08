package com.restaurant.billservice.kafka.consumer;

import com.restaurant.billservice.service.BillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedConsumer {

    private final BillService billService;

    @KafkaListener(
            topics = "${kafka.topic.order-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrder(
            @Payload List<OrderCreatedEvent> events,
            Acknowledgment acknowledgment) {

        log.info("Processing batch of {} order-created events", events.size());

        try {

            billService.processOrderCreatedEvents(events);

            acknowledgment.acknowledge();

            log.info(
                    "Successfully processed batch of {} order-created events",
                    events.size()
            );

        } catch (Exception ex) {

            log.error(
                    "Failed to process order-created batch of {} events",
                    events.size(),
                    ex
            );

            throw ex;
        }
    }
}
