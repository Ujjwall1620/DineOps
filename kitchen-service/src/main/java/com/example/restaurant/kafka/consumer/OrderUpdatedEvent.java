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
public class OrderUpdatedEvent {

    private Long   orderId;
    private Long   restaurantId;
    private String orderNumber;
    private LocalDateTime eventTimestamp;

    // Poori/current item list hamesha bhejo (diff nahi) — jaisa humne discuss kiya tha,
    // isse consumer side "replace" logic simple aur safe rehta hai
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