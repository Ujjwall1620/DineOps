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
import com.restaurant.menuservice.service.MenuItemMapper;
import com.restaurant.menuservice.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final MenuItemMapper     menuItemMapper;

    // ─── Create ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public MenuItemResponse createMenuItem(CreateMenuItemRequest request) {
        log.info("Creating menu item: {}", request.getName());

        if (menuItemRepository.existsByNameIgnoreCase(request.getName())) {
            throw new MenuAlreadyExistsException(request.getName());
        }

        MenuItem item = MenuItem.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .category(request.getCategory())
                .price(request.getPrice())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .imageUrl(request.getImageUrl())
                .build();

        MenuItem saved = menuItemRepository.save(item);
        log.info("Menu item created with id: {}", saved.getId());


        return menuItemMapper.toMenuItemResponse(saved);
    }

    // ─── Update ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public MenuItemResponse updateMenuItem(Long id, UpdateMenuItemRequest request) {
        log.info("Updating menu item id: {}", id);

        MenuItem item = findById(id);

        // Partial update: only apply non-null fields
        if (StringUtils.hasText(request.getName())) {
            String newName = request.getName().trim();
            // Guard against duplicate name only if the name is actually changing
            if (!newName.equalsIgnoreCase(item.getName())
                    && menuItemRepository.existsByNameIgnoreCase(newName)) {
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
        log.info("Menu item updated: {}", saved.getId());


        return menuItemMapper.toMenuItemResponse(saved);
    }

    // ─── Delete ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteMenuItem(Long id) {
        log.info("Deleting menu item id: {}", id);
        MenuItem item = findById(id);
        menuItemRepository.delete(item);
        log.info("Menu item deleted: {}", id);

    }

    // ─── Toggle Availability ───────────────────────────────────────────────────

    @Override
    @Transactional
    public MenuItemResponse updateAvailability(Long id, AvailabilityRequest request) {
        log.info("Updating availability for menu item id: {} → {}", id, request.getAvailable());
        MenuItem item = findById(id);
        item.setAvailable(request.getAvailable());
        MenuItem saved = menuItemRepository.save(item);
        return menuItemMapper.toMenuItemResponse(saved);
    }

    // ─── Read ──────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getMenuItemById(Long id) {
        return menuItemMapper.toMenuItemResponse(findById(id));
    }

    /**
     * Called indirectly by Order Service via GET /api/menu/{id}.
     * Returns the slim {@link MenuItemSummaryResponse} contract.
     */
    @Override
    @Transactional(readOnly = true)
    public MenuItemSummaryResponse getMenuItemSummaryById(Long id) {
        return menuItemMapper.toMenuItemSummaryResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAllMenuItems() {
        return menuItemMapper.toMenuItemResponseList(menuItemRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getMenuItemsByCategory(MenuCategory category) {
        return menuItemMapper.toMenuItemResponseList(
                menuItemRepository.findByCategory(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> getAvailableMenuItems() {
        return menuItemMapper.toMenuItemResponseList(
                menuItemRepository.findAllAvailableOrderByCategoryAndName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> searchMenuItemsByName(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllMenuItems();
        }
        return menuItemMapper.toMenuItemResponseList(
                menuItemRepository.searchByName(keyword.trim()));
    }

    // ─── Helper ────────────────────────────────────────────────────────────────

    private MenuItem findById(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new MenuItemNotFoundException(id));
    }
}
