package com.restaurant.orderservice.exception;

public class MenuServiceUnavailableException extends RuntimeException {
    public MenuServiceUnavailableException(String message) {
        super(message);
    }
}
