package com.restaurant.billservice.exception;

public class KafkaPublishException extends RuntimeException {
    public KafkaPublishException(String message) { super(message); }
}
