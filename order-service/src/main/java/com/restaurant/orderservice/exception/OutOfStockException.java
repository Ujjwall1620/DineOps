package com.restaurant.orderservice.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }
    public OutOfStockException(String itemName, int requested, int available) {
        super(String.format("'%s' is out of stock. Requested: %d, Available: %d",
                itemName, requested, available));
    }
}
