package com.restaurant.menuservice.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.menu-created}")
    private String menuCreatedTopic;

    @Value("${kafka.topic.menu-updated}")
    private String menuUpdatedTopic;

    @Value("${kafka.topic.menu-deleted}")
    private String menuDeletedTopic;

    @Value("${kafka.topic.menu-availability-changed}")
    private String menuAvailabilityChangedTopic;

    @Bean
    public NewTopic menuCreatedTopic() {
        return TopicBuilder.name(menuCreatedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic menuUpdatedTopic() {
        return TopicBuilder.name(menuUpdatedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic menuDeletedTopic() {
        return TopicBuilder.name(menuDeletedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic menuAvailabilityChangedTopic() {
        return TopicBuilder.name(menuAvailabilityChangedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public KafkaTemplate<String, MenuEvent> menuEventKafkaTemplate(
            ProducerFactory<String, MenuEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
