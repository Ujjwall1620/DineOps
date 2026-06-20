package com.restaurant.kitchenservice.kafka.producer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event published by Kitchen Service on topics:
 * - order-cooking-started  (when chef starts preparation)
 * - order-ready            (when chef marks food ready)
 *
 * Order Service consumes these to update the Order's own status.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KitchenStatusEvent {

    private Long    orderId;
    private String  orderNumber;
    private Integer tableNumber;
    private String  status;        // "IN_PREPARATION" | "READY"
    private Long    chefId;
    private String  chefName;
    private String  eventType;     // "ORDER_COOKING_STARTED" | "ORDER_READY"
    private LocalDateTime eventTimestamp;
}
