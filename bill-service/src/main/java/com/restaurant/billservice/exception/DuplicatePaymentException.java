package com.restaurant.billservice.exception;

public class DuplicatePaymentException extends RuntimeException {
    public DuplicatePaymentException(String idempotencyKey) {
        super("Duplicate payment request detected for idempotency key: " + idempotencyKey);
    }
}
