package com.restaurant.menuservice.exception;

public class MenuAlreadyExistsException extends RuntimeException {
    public MenuAlreadyExistsException(String name) {
        super("A menu item with the name '" + name + "' already exists.");
    }
}
