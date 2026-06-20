package com.restaurant.kitchenservice.config;

import com.restaurant.kitchenservice.kafka.producer.KitchenStatusEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.order-ready}")
    private String orderReadyTopic;

    @Value("${kafka.topic.order-cooking-started}")
    private String orderCookingStartedTopic;

    // ─── Auto-create topics on startup ────────────────────────────────────────

    @Bean
    public NewTopic orderReadyTopic() {
        return TopicBuilder.name(orderReadyTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderCookingStartedTopic() {
        return TopicBuilder.name(orderCookingStartedTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    // ─── Typed Kafka template ─────────────────────────────────────────────────

    @Bean
    public KafkaTemplate<String, KitchenStatusEvent> kitchenStatusKafkaTemplate(
            ProducerFactory<String, KitchenStatusEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
