package com.example.restaurant.exception;

import com.example.restaurant.enums.KitchenStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(KitchenStatus from, KitchenStatus to) {
        super("Invalid status transition: " + from + " → " + to);
    }
}
