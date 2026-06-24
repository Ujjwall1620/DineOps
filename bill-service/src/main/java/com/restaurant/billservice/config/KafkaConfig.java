package com.restaurant.billservice.config;

import com.restaurant.billservice.kafka.producer.BillGeneratedEvent;
import com.restaurant.billservice.kafka.producer.PaymentStatusEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.bill-generated}")
    private String billGeneratedTopic;

    @Value("${kafka.topic.payment-completed}")
    private String paymentCompletedTopic;

    @Value("${kafka.topic.payment-failed}")
    private String paymentFailedTopic;

    // ─── Auto-create topics on startup ────────────────────────────────────────

    @Bean
    public NewTopic billGeneratedTopic() {
        return TopicBuilder.name(billGeneratedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(paymentCompletedTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(paymentFailedTopic).partitions(3).replicas(1).build();
    }

    // ─── Typed Kafka templates — same pattern as Kitchen Service ──────────────

    @Bean
    public KafkaTemplate<String, BillGeneratedEvent> billGeneratedKafkaTemplate(
            ProducerFactory<String, BillGeneratedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, PaymentStatusEvent> paymentStatusKafkaTemplate(
            ProducerFactory<String, PaymentStatusEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
