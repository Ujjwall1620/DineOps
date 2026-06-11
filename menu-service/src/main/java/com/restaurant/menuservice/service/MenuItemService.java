package com.restaurant.menuservice.service;

import com.restaurant.menuservice.dto.request.AvailabilityRequest;
import com.restaurant.menuservice.dto.request.CreateMenuItemRequest;
import com.restaurant.menuservice.dto.request.UpdateMenuItemRequest;
import com.restaurant.menuservice.dto.response.MenuItemResponse;
import com.restaurant.menuservice.dto.response.MenuItemSummaryResponse;
import com.restaurant.menuservice.enums.MenuCategory;

import java.util.List;

public interface MenuItemService {

    // ─── Write Operations ──────────────────────────────────────────────────────
    MenuItemResponse createMenuItem(CreateMenuItemRequest request);
    MenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request);
    void             deleteMenuItem(Long id);
    MenuItemResponse updateAvailability(Long id, AvailabilityRequest request);

    // ─── Read Operations ───────────────────────────────────────────────────────
    MenuItemResponse        getMenuItemById(Long id);
    MenuItemSummaryResponse getMenuItemSummaryById(Long id);   // consumed by Order Service
    List<MenuItemResponse>  getAllMenuItems();
    List<MenuItemResponse>  getMenuItemsByCategory(MenuCategory category);
    List<MenuItemResponse>  getAvailableMenuItems();
    List<MenuItemResponse>  searchMenuItemsByName(String keyword);
}
