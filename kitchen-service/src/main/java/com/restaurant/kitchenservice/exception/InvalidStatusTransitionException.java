package com.restaurant.kitchenservice.exception;

import com.restaurant.kitchenservice.enums.KitchenStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(KitchenStatus from, KitchenStatus to) {
        super("Invalid status transition: " + from + " → " + to);
    }
}
