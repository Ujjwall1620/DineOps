package com.restaurant.billservice.kafka.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mirrors the KitchenStatusEvent published by Kitchen Service on topic {@code order-ready}.
 *
 * IMPORTANT: The Kitchen Service's KitchenStatusEvent does NOT carry item details
 * or prices. The Bill Service must therefore consume the richer order-ready event
 * that Order Service publishes (which includes items with frozen prices).
 *
 * Design decision: Bill Service listens to Order Service's enriched event
 * on topic {@code order-ready} (published by Order Service when it receives
 * the kitchen order-ready signal and updates order status to READY).
 *
 * This gives us: orderId, orderNumber, tableNumber, waiterId, waiterName,
 * items with price_per_unit (frozen at order time), and totalAmount.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReadyEvent {

    private Long    orderId;
    private String  orderNumber;
    private Integer tableNumber;
    private Long    waiterId;
    private String  waiterName;
    private String  status;
    private BigDecimal totalAmount;
    private String  eventType;
    private LocalDateTime eventTimestamp;

    private List<OrderItemPayload> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemPayload {
        private Long       menuItemId;
        private String     menuItemName;
        private Integer    quantity;
        private BigDecimal pricePerUnit;   // frozen price from Order Service
        private BigDecimal subtotal;
    }
}
