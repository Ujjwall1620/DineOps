package com.restaurant.menuservice.kafka;

import com.restaurant.menuservice.entity.MenuItem;
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
public class MenuProducer {

    private final KafkaTemplate<String, MenuEvent> kafkaTemplate;

    @Value("${kafka.topic.menu-created}")
    private String menuCreatedTopic;

    @Value("${kafka.topic.menu-updated}")
    private String menuUpdatedTopic;

    @Value("${kafka.topic.menu-deleted}")
    private String menuDeletedTopic;

    @Value("${kafka.topic.menu-availability-changed}")
    private String menuAvailabilityChangedTopic;

    public void publishMenuCreated(MenuItem item) {
        send(menuCreatedTopic, buildEvent(item, "MENU_CREATED"));
        log.info("Published MENU_CREATED event for item id: {}", item.getId());
    }

    public void publishMenuUpdated(MenuItem item) {
        send(menuUpdatedTopic, buildEvent(item, "MENU_UPDATED"));
        log.info("Published MENU_UPDATED event for item id: {}", item.getId());
    }

    public void publishMenuDeleted(MenuItem item) {
        send(menuDeletedTopic, buildEvent(item, "MENU_DELETED"));
        log.info("Published MENU_DELETED event for item id: {}", item.getId());
    }

    public void publishAvailabilityChanged(MenuItem item) {
        send(menuAvailabilityChangedTopic, buildEvent(item, "MENU_AVAILABILITY_CHANGED"));
        log.info("Published MENU_AVAILABILITY_CHANGED event for item id: {}, available: {}",
                item.getId(), item.getAvailable());
    }

    private void send(String topic, MenuEvent event) {
        CompletableFuture<SendResult<String, MenuEvent>> future =
                kafkaTemplate.send(topic, event.getMenuItemId().toString(), event);

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

    private MenuEvent buildEvent(MenuItem item, String eventType) {
        return MenuEvent.builder()
                .menuItemId(item.getId())
                .name(item.getName())
                .category(item.getCategory())
                .price(item.getPrice())
                .available(item.getAvailable())
                .eventType(eventType)
                .eventTimestamp(LocalDateTime.now())
                .build();
    }
}
