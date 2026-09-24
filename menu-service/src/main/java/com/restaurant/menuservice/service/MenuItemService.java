package com.restaurant.menuservice.service;

import com.restaurant.menuservice.dto.request.AvailabilityRequest;
import com.restaurant.menuservice.dto.request.CreateMenuItemRequest;
import com.restaurant.menuservice.dto.request.UpdateMenuItemRequest;
import com.restaurant.menuservice.dto.response.MenuItemResponse;
import com.restaurant.menuservice.dto.response.MenuItemSummaryResponse;
import com.restaurant.menuservice.enums.MenuCategory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MenuItemService {



    MenuItemResponse createMenuItem(
            CreateMenuItemRequest request
    );


    MenuItemResponse updateMenuItem(
            Long id,
            UpdateMenuItemRequest request
    );


    void deleteMenuItem(Long id);


    MenuItemResponse updateAvailability(
            Long id,
            AvailabilityRequest request
    );


    MenuItemResponse getMenuItemById(Long id);

    MenuItemSummaryResponse getMenuItemSummaryById(Long id);

    List<MenuItemResponse> getAllMenuItems();

    List<MenuItemResponse> getMenuItemsByCategory(
            MenuCategory category
    );


    List<MenuItemResponse> getAvailableMenuItems();

    List<MenuItemResponse> searchMenuItemsByName(
            String keyword
    );
}