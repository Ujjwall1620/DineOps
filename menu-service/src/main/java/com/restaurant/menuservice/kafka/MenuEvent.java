package com.restaurant.menuservice.kafka;

import com.restaurant.menuservice.enums.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuEvent {

    private Long menuItemId;
    private String name;
    private MenuCategory category;
    private BigDecimal price;
    private Boolean available;

    /**
     * One of: MENU_CREATED, MENU_UPDATED, MENU_DELETED, MENU_AVAILABILITY_CHANGED
     */
    private String eventType;

    private LocalDateTime eventTimestamp;
}
