package com.restaurant.menuservice.service;

import com.restaurant.menuservice.dto.request.AvailabilityRequest;
import com.restaurant.menuservice.dto.request.CreateMenuItemRequest;
import com.restaurant.menuservice.dto.request.UpdateMenuItemRequest;
import com.restaurant.menuservice.dto.response.MenuItemResponse;
import com.restaurant.menuservice.dto.response.MenuItemSummaryResponse;
import com.restaurant.menuservice.enums.MenuCategory;

import java.util.List;

public interface MenuItemService {

    MenuItemResponse createMenuItem(
            CreateMenuItemRequest request,
            String token
    );

    MenuItemResponse updateMenuItem(
            Long id,
            UpdateMenuItemRequest request,
            String token
    );

    void deleteMenuItem(
            Long id,
            String token
    );

    MenuItemResponse updateAvailability(
            Long id,
            AvailabilityRequest request,
            String token
    );

    MenuItemResponse getMenuItemById(
            Long id,
            String token
    );

    MenuItemSummaryResponse getMenuItemSummaryById(
            Long id,
            String token
    );

    List<MenuItemResponse> getAllMenuItems(
            String token
    );

    List<MenuItemResponse> getMenuItemsByCategory(
            MenuCategory category,
            String token
    );

    List<MenuItemResponse> getAvailableMenuItems(
            String token
    );

    List<MenuItemResponse> searchMenuItemsByName(
            String keyword,
            String token
    );
}