package com.restaurant.orderservice.client;

import com.restaurant.orderservice.dto.response.MenuItemResponse;
import com.restaurant.orderservice.exception.MenuServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MenuClientFallback implements MenuClient {

    @Override
    public MenuItemResponse getMenuItemById(Long id) {
        log.error("Menu service is unavailable. Fallback triggered for menuItemId: {}", id);
        throw new MenuServiceUnavailableException(
                "Menu service is currently unavailable. Please try again later.");
    }
}
