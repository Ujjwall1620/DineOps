package com.restaurant.menuservice.controller;

import com.restaurant.menuservice.dto.request.AvailabilityRequest;
import com.restaurant.menuservice.dto.request.CreateMenuItemRequest;
import com.restaurant.menuservice.dto.request.UpdateMenuItemRequest;
import com.restaurant.menuservice.dto.response.ApiResponse;
import com.restaurant.menuservice.dto.response.MenuItemResponse;
import com.restaurant.menuservice.dto.response.MenuItemSummaryResponse;
import com.restaurant.menuservice.enums.MenuCategory;
import com.restaurant.menuservice.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuItemController {

    private final MenuItemService menuItemService;

    // ─────────────────────────────────────────────────────────────────────────
    // WRITE OPERATIONS — restricted to MANAGER/ADMIN roles
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * POST /api/menu
     * Create a new menu item.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(
            @Valid @RequestBody CreateMenuItemRequest request) {
        log.info("POST /api/menu - name: {}", request.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Menu item created successfully",
                        menuItemService.createMenuItem(request)));
    }

    /**
     * PUT /api/menu/{id}
     * Full or partial update of a menu item.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        log.info("PUT /api/menu/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated successfully",
                menuItemService.updateMenuItem(id, request)));
    }

    /**
     * DELETE /api/menu/{id}
     * Hard-delete a menu item.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMenuItem(@PathVariable Long id) {
        log.info("DELETE /api/menu/{}", id);
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted successfully"));
    }

    /**
     * PATCH /api/menu/{id}/availability
     * Toggle availability without a full update.
     */
    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('ADMIN','WAITER')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateAvailability(
            @PathVariable Long id,
            @Valid @RequestBody AvailabilityRequest request) {
        log.info("PATCH /api/menu/{}/availability → {}", id, request.getAvailable());
        return ResponseEntity.ok(ApiResponse.success("Availability updated",
                menuItemService.updateAvailability(id, request)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // READ OPERATIONS — all authenticated users (including Order Service)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * GET /api/menu/{id}
     *
     * THIS IS THE CONTRACT CONSUMED BY ORDER SERVICE via Feign client.
     * Returns {@link MenuItemSummaryResponse} with id, name, price, availableStock.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuItemSummaryResponse> getMenuItemSummary(@PathVariable Long id) {
        log.debug("GET /api/menu/{} (summary for Order Service)", id);
        return ResponseEntity.ok(menuItemService.getMenuItemSummaryById(id));
    }

    /**
     * GET /api/menu/{id}/detail
     * Full item details for admin/management UIs.
     */
    @GetMapping("/{id}/detail")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItemDetail(@PathVariable Long id) {
        log.debug("GET /api/menu/{}/detail", id);
        return ResponseEntity.ok(ApiResponse.success("Menu item retrieved",
                menuItemService.getMenuItemById(id)));
    }

    /**
     * GET /api/menu
     * All menu items (admin view — includes unavailable).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getAllMenuItems() {
        log.debug("GET /api/menu");
        return ResponseEntity.ok(ApiResponse.success("All menu items retrieved",
                menuItemService.getAllMenuItems()));
    }

    /**
     * GET /api/menu/available
     * Only available items — customer-facing / Order Service reference.
     */
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getAvailableItems() {
        log.debug("GET /api/menu/available");
        return ResponseEntity.ok(ApiResponse.success("Available menu items retrieved",
                menuItemService.getAvailableMenuItems()));
    }

    /**
     * GET /api/menu/category/{category}
     * Filter by category.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getByCategory(
            @PathVariable MenuCategory category) {
        log.debug("GET /api/menu/category/{}", category);
        return ResponseEntity.ok(ApiResponse.success("Menu items by category retrieved",
                menuItemService.getMenuItemsByCategory(category)));
    }

    /**
     * GET /api/menu/search?keyword=burger
     * Name-based partial search.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> searchByName(
            @RequestParam(required = false, defaultValue = "") String keyword) {
        log.debug("GET /api/menu/search?keyword={}", keyword);
        return ResponseEntity.ok(ApiResponse.success("Search results",
                menuItemService.searchMenuItemsByName(keyword)));
    }
}
