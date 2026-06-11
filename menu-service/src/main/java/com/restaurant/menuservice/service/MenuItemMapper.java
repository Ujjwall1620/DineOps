package com.restaurant.menuservice.service;

import com.restaurant.menuservice.dto.response.MenuItemResponse;
import com.restaurant.menuservice.dto.response.MenuItemSummaryResponse;
import com.restaurant.menuservice.entity.MenuItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class MenuItemMapper {

    public MenuItemResponse toMenuItemResponse(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .category(item.getCategory())
                .price(item.getPrice())
                .available(item.getAvailable())
                .imageUrl(item.getImageUrl())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    public List<MenuItemResponse> toMenuItemResponseList(List<MenuItem> items) {
        return items.stream().map(this::toMenuItemResponse).collect(Collectors.toList());
    }

    /**
     * Maps to the lightweight DTO consumed by Order Service via Feign.
     * available=true  → availableStock = Integer.MAX_VALUE (always passes stock check)
     * available=false → availableStock = 0               (always fails stock check)
     */
    public MenuItemSummaryResponse toMenuItemSummaryResponse(MenuItem item) {
        int stock = Boolean.TRUE.equals(item.getAvailable()) ? Integer.MAX_VALUE : 0;
        return MenuItemSummaryResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .availableStock(stock)
                .build();
    }
}
