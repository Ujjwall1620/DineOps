package com.restaurant.orderservice.client;

import com.restaurant.orderservice.dto.response.MenuItemResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "menu-service",
        url = "${menu.service.url}",
        configuration = FeignJwtConfig.class,
        fallback = MenuClientFallback.class
)
public interface MenuClient {

    @GetMapping("/api/menu/{id}")
    MenuItemResponse getMenuItemById(@PathVariable("id") Long id);
}
