package com.restaurant.orderservice.kafka;

import com.restaurant.orderservice.enums.OrderStatus;
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
public class OrderEvent {

    private Long orderId;
    private String orderNumber;
    private Integer tableNumber;
    private Long waiterId;
    private String waiterEmail;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String eventType;   // ORDER_CREATED, ORDER_UPDATED, ORDER_CANCELLED
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
