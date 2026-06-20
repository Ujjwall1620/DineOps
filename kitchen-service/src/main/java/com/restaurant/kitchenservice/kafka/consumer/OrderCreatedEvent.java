package com.restaurant.kitchenservice.kafka.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirrors the OrderEvent published by Order Service on topic {@code order-created}.
 * Field names must match exactly for Jackson deserialization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private Long   orderId;
    private String orderNumber;
    private Integer tableNumber;
    private Long   waiterId;
    private String waiterName;
    private String status;          // "PENDING"
    private LocalDateTime eventTimestamp;

    private List<OrderItemPayload> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemPayload {
        private Long   menuItemId;
        private String menuItemName;
        private Integer quantity;
    }
}
