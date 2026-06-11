package com.restaurant.menuservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Lightweight projection returned by GET /api/menu/{id}.
 *
 * This is the exact contract the Order Service's MenuClient expects:
 * <pre>
 * {
 *   "id": 1,
 *   "name": "Burger",
 *   "price": 150.00,
 *   "availableStock": 20
 * }
 * </pre>
 *
 * NOTE: "availableStock" is kept as a fixed positive integer to satisfy the
 * Order Service stock-check contract (availableStock > requestedQty).
 * In a real system this would come from an Inventory Service; here it is
 * derived from the {@code available} flag: available=true → MAX_VALUE,
 * available=false → 0.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemSummaryResponse {

    private Long id;
    private String name;
    private BigDecimal price;

    /**
     * Exposed as availableStock to match the field name the Order Service
     * Feign client deserializes. When available=true, returns Integer.MAX_VALUE
     * so any order quantity passes the stock guard. When available=false,
     * returns 0 so Order Service throws OutOfStockException automatically.
     */
    private Integer availableStock;
}
