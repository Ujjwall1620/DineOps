package com.restaurant.menuservice.exception;

public class InvalidMenuItemException extends RuntimeException {
    public InvalidMenuItemException(String message) {
        super(message);
    }
}
