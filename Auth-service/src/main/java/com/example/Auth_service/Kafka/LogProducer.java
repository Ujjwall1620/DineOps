package com.example.Auth_service.Kafka;

import com.example.Auth_service.DTO.LogMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogProducer {
    private final KafkaTemplate<String, LogMessage> kafkaTemplate;

    public void sendLog(LogMessage logMessage){
        kafkaTemplate.send("log-collection-topic", logMessage);
    }
}
