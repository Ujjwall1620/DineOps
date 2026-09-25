package com.restaurant.kitchenservice.exception;

public class KitchenTicketNotFoundException extends RuntimeException {
    public KitchenTicketNotFoundException(Long id) {
        super("Kitchen ticket not found with id: " + id);
    }
    public KitchenTicketNotFoundException(String message) {
        super(message);
    }
}
