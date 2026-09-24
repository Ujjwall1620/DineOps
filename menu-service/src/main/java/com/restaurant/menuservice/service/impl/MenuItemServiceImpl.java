package com.restaurant.menuservice.service.impl;

import com.restaurant.menuservice.dto.request.AvailabilityRequest;
import com.restaurant.menuservice.dto.request.CreateMenuItemRequest;
import com.restaurant.menuservice.dto.request.UpdateMenuItemRequest;
import com.restaurant.menuservice.dto.response.MenuItemResponse;
import com.restaurant.menuservice.dto.response.MenuItemSummaryResponse;
import com.restaurant.menuservice.entity.MenuItem;
import com.restaurant.menuservice.enums.MenuCategory;
import com.restaurant.menuservice.exception.MenuAlreadyExistsException;
import com.restaurant.menuservice.exception.MenuItemNotFoundException;
import com.restaurant.menuservice.repository.MenuItemRepository;
import com.restaurant.menuservice.security.JwtUserDetails;
import com.restaurant.menuservice.service.MenuItemMapper;
import com.restaurant.menuservice.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemMapper menuItemMapper;


    // ============================================================
    // GET CURRENT USER
    // ============================================================

    private JwtUserDetails getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return (JwtUserDetails) authentication.getPrincipal();
    }


    // ============================================================
    // CREATE
    // ============================================================

    @Transactional
    @Override
    public MenuItemResponse createMenuItem(
            CreateMenuItemRequest request
    ) {

        Long restaurantId = getCurrentUser().getRestaurantId();

        log.info(
                "Creating menu item: {} for restaurantId: {}",
                request.getName(),
                restaurantId
        );

        // Check duplicate name only inside this restaurant
        if (menuItemRepository.existsByNameIgnoreCaseAndRestaurantId(
                request.getName().trim(),
                restaurantId
        )) {
            throw new MenuAlreadyExistsException(request.getName());
        }

        MenuItem item = MenuItem.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .available(
                        request.getAvailable() != null
                                ? request.getAvailable()
                                : true
                )
                .imageUrl(request.getImageUrl())
                .restaurantId(restaurantId)
                .build();

        MenuItem saved = menuItemRepository.save(item);

        log.info(
                "Menu item created with id: {} for restaurantId: {}",
                saved.getId(),
                restaurantId
        );

        return menuItemMapper.toMenuItemResponse(saved);
    }


    // ============================================================
    // UPDATE
    // ============================================================

    @Transactional
    @Override
    public MenuItemResponse updateMenuItem(
            Long id,
            UpdateMenuItemRequest request
    ) {

        Long restaurantId = getCurrentUser().getRestaurantId();

        log.info(
                "Updating menu item id: {} for restaurantId: {}",
                id,
                restaurantId
        );

        MenuItem item =
                findByIdAndRestaurantId(id, restaurantId);

        if (StringUtils.hasText(request.getName())) {

            String newName = request.getName().trim();

            // Check duplicate only when name is actually changing
            if (!newName.equalsIgnoreCase(item.getName())
                    && menuItemRepository
                    .existsByNameIgnoreCaseAndRestaurantId(
                            newName,
                            restaurantId
                    )) {

                throw new MenuAlreadyExistsException(newName);
            }

            item.setName(newName);
        }

        if (request.getDescription() != null) {
            item.setDescription(request.getDescription());
        }

        if (request.getCategory() != null) {
            item.setCategory(request.getCategory());
        }

        if (request.getPrice() != null) {
            item.setPrice(request.getPrice());
        }

        if (request.getAvailable() != null) {
            item.setAvailable(request.getAvailable());
        }

        if (request.getImageUrl() != null) {
            item.setImageUrl(request.getImageUrl());
        }

        MenuItem saved = menuItemRepository.save(item);

        log.info(
                "Menu item updated: {} for restaurantId: {}",
                saved.getId(),
                restaurantId
        );

        return menuItemMapper.toMenuItemResponse(saved);
    }


    // ============================================================
    // DELETE
    // ============================================================

    @Transactional
    @Override
    public void deleteMenuItem(Long id) {

        Long restaurantId = getCurrentUser().getRestaurantId();

        log.info(
                "Deleting menu item id: {} for restaurantId: {}",
                id,
                restaurantId
        );

        // Only delete if menu item belongs to this restaurant
        MenuItem item =
                findByIdAndRestaurantId(id, restaurantId);

        menuItemRepository.delete(item);

        log.info(
                "Menu item deleted: {} from restaurantId: {}",
                id,
                restaurantId
        );
    }


    // ============================================================
    // UPDATE AVAILABILITY
    // ============================================================

    @Transactional
    @Override
    public MenuItemResponse updateAvailability(
            Long id,
            AvailabilityRequest request
    ) {

        Long restaurantId = getCurrentUser().getRestaurantId();

        log.info(
                "Updating availability for menu item id: {} for restaurantId: {} → {}",
                id,
                restaurantId,
                request.getAvailable()
        );

        MenuItem item =
                findByIdAndRestaurantId(id, restaurantId);

        item.setAvailable(request.getAvailable());

        MenuItem saved = menuItemRepository.save(item);

        return menuItemMapper.toMenuItemResponse(saved);
    }


    // ============================================================
    // GET BY ID
    // ============================================================

    @Transactional(readOnly = true)
    @Override
    public MenuItemResponse getMenuItemById(Long id) {

        Long restaurantId = getCurrentUser().getRestaurantId();

        MenuItem item =
                findByIdAndRestaurantId(id, restaurantId);

        return menuItemMapper.toMenuItemResponse(item);
    }


    // ============================================================
    // GET SUMMARY BY ID
    // ============================================================

    @Transactional(readOnly = true)
    @Override
    public MenuItemSummaryResponse getMenuItemSummaryById(Long id) {

        Long restaurantId = getCurrentUser().getRestaurantId();

        MenuItem item =
                findByIdAndRestaurantId(id, restaurantId);

        return menuItemMapper.toMenuItemSummaryResponse(item);
    }


    // ============================================================
    // GET ALL MENU ITEMS
    // ============================================================

    @Transactional(readOnly = true)
    @Override
    public List<MenuItemResponse> getAllMenuItems() {

        Long restaurantId = getCurrentUser().getRestaurantId();

        return menuItemMapper.toMenuItemResponseList(
                menuItemRepository.findAllByRestaurantId(restaurantId)
        );
    }


    // ============================================================
    // GET BY CATEGORY
    // ============================================================

    @Transactional(readOnly = true)
    @Override
    public List<MenuItemResponse> getMenuItemsByCategory(
            MenuCategory category
    ) {

        Long restaurantId = getCurrentUser().getRestaurantId();

        return menuItemMapper.toMenuItemResponseList(
                menuItemRepository.findByCategoryAndRestaurantId(
                        category,
                        restaurantId
                )
        );
    }


    // ============================================================
    // GET AVAILABLE MENU
    // ============================================================

    @Transactional(readOnly = true)
    @Override
    public List<MenuItemResponse> getAvailableMenuItems() {

        Long restaurantId = getCurrentUser().getRestaurantId();

        return menuItemMapper.toMenuItemResponseList(
                menuItemRepository
                        .findAllAvailableOrderByCategoryAndName(
                                restaurantId
                        )
        );
    }


    // ============================================================
    // SEARCH BY NAME
    // ============================================================

    @Transactional(readOnly = true)
    @Override
    public List<MenuItemResponse> searchMenuItemsByName(
            String keyword
    ) {

        Long restaurantId = getCurrentUser().getRestaurantId();

        if (!StringUtils.hasText(keyword)) {

            return menuItemMapper.toMenuItemResponseList(
                    menuItemRepository.findAllByRestaurantId(
                            restaurantId
                    )
            );
        }

        return menuItemMapper.toMenuItemResponseList(
                menuItemRepository.searchByName(
                        keyword.trim(),
                        restaurantId
                )
        );
    }


    // ============================================================
    // HELPER
    // ============================================================

    private MenuItem findByIdAndRestaurantId(
            Long id,
            Long restaurantId
    ) {

        return menuItemRepository
                .findByIdAndRestaurantId(id, restaurantId)
                .orElseThrow(
                        () -> new MenuItemNotFoundException(id)
                );
    }
}

