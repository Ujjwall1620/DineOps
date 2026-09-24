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


    // =========================
    // CREATE
    // =========================

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> createMenuItem(

            @Valid @RequestBody CreateMenuItemRequest request,

            @RequestHeader("Authorization") String token
    ) {

        log.info(
                "POST /api/menu - name: {}",
                request.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.created(
                                "Menu item created successfully",

                                menuItemService.createMenuItem(
                                        request
                                )
                        )
                );
    }


    // =========================
    // UPDATE
    // =========================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateMenuItem(

            @PathVariable Long id,

            @Valid @RequestBody UpdateMenuItemRequest request,

            @RequestHeader("Authorization") String token
    ) {

        log.info(
                "PUT /api/menu/{}",
                id
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Menu item updated successfully",

                        menuItemService.updateMenuItem(
                                id,
                                request
                        )
                )
        );
    }


    // =========================
    // DELETE
    // =========================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<String> deleteMenuItem(

            @PathVariable Long id,

            @RequestHeader("Authorization") String token
    ) {

        log.info(
                "DELETE /api/menu/{}",
                id
        );

        menuItemService.deleteMenuItem(
                id
        );

        return ResponseEntity.ok("Menu item deleted successfully");
    }


    // =========================
    // UPDATE AVAILABILITY
    // =========================

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('OWNER', 'MANAGER')")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateAvailability(

            @PathVariable Long id,

            @Valid @RequestBody AvailabilityRequest request,

            @RequestHeader("Authorization") String token
    ) {

        log.info(
                "PATCH /api/menu/{}/availability -> {}",
                id,
                request.getAvailable()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Availability updated",

                        menuItemService.updateAvailability(
                                id,
                                request
                        )
                )
        );
    }


    // =========================
    // GET SUMMARY
    // =========================

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemSummaryResponse> getMenuItemSummary(

            @PathVariable Long id,

            @RequestHeader("Authorization") String token
    ) {

        log.debug(
                "GET /api/menu/{}",
                id
        );

        return ResponseEntity.ok(
                menuItemService.getMenuItemSummaryById(
                        id
                )
        );
    }


    // =========================
    // GET DETAIL
    // =========================

    @GetMapping("/{id}/detail")
    public ResponseEntity<ApiResponse<MenuItemResponse>> getMenuItemDetail(

            @PathVariable Long id,

            @RequestHeader("Authorization") String token
    ) {

        log.debug(
                "GET /api/menu/{}/detail",
                id
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Menu item retrieved",

                        menuItemService.getMenuItemById(
                                id
                        )
                )
        );
    }


    // =========================
    // GET ALL
    // =========================

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getAllMenuItems(

            @RequestHeader("Authorization") String token
    ) {

        log.debug(
                "GET /api/menu"
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All menu items retrieved",

                        menuItemService.getAllMenuItems()
                )
        );
    }


    // =========================
    // GET AVAILABLE
    // =========================

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getAvailableItems(

            @RequestHeader("Authorization") String token
    ) {

        log.debug(
                "GET /api/menu/available"
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Available menu items retrieved",

                        menuItemService.getAvailableMenuItems()
                )
        );
    }


    // =========================
    // GET BY CATEGORY
    // =========================

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getByCategory(

            @PathVariable MenuCategory category,

            @RequestHeader("Authorization") String token
    ) {

        log.debug(
                "GET /api/menu/category/{}",
                category
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Menu items by category retrieved",

                        menuItemService.getMenuItemsByCategory(
                                category
                        )
                )
        );
    }


    // =========================
    // SEARCH
    // =========================

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> searchByName(

            @RequestParam(
                    required = false,
                    defaultValue = ""
            )
            String keyword,

            @RequestHeader("Authorization") String token
    ) {

        log.debug("GET /api/menu/search?keyword={}", keyword);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Search results",

                        menuItemService.searchMenuItemsByName(keyword)
                )
        );
    }
}