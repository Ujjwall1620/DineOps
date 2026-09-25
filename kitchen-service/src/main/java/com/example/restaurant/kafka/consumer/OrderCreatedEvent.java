package com.example.restaurant.kafka.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private Long   orderId;
    private Long   restaurantId;
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
