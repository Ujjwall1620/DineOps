package com.example.restaurant.kafka.consumer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent {

    private Long   orderId;
    private Long   restaurantId;
    private String orderNumber;
    private String reason;              // optional — "customer cancelled", "kitchen rejected" waghera
    private LocalDateTime eventTimestamp;
}