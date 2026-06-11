package com.restaurant.orderservice.exception;

public class MenuItemNotFoundException extends RuntimeException {
    public MenuItemNotFoundException(Long menuItemId) {
        super("Menu item not found with id: " + menuItemId);
    }
}
