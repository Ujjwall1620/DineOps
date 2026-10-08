package com.restaurant.billservice.kafka.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {

    private Long orderId;
    private Long restaurantId;
    private String orderNumber;
    private Integer tableNumber;

    private Long waiterId;
    private String waiterEmail;

    private String status;
    private BigDecimal totalAmount;

    private String eventType;
    private LocalDateTime eventTimestamp;

    private List<OrderItemEvent> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {

        private Long menuItemId;
        private String menuItemName;
        private Integer quantity;

        private BigDecimal pricePerUnit;
        private BigDecimal subtotal;
    }
}