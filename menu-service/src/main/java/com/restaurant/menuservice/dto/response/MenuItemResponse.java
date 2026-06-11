package com.restaurant.menuservice.dto.response;

import com.restaurant.menuservice.enums.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Full menu item representation returned to internal consumers
 * (admin, management endpoints).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuItemResponse {

    private Long id;
    private String name;
    private String description;
    private MenuCategory category;
    private BigDecimal price;
    private Boolean available;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
