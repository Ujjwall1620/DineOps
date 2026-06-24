package com.restaurant.billservice.exception;

public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String message) { super(message); }
}
