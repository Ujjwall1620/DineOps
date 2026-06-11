package com.restaurant.menuservice.exception;

public class MenuItemNotFoundException extends RuntimeException {
    public MenuItemNotFoundException(Long id) {
        super("Menu item not found with id: " + id);
    }
    public MenuItemNotFoundException(String name) {
        super("Menu item not found with name: " + name);
    }
}
