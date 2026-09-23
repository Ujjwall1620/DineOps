package com.restaurant.menuservice.repository;

import com.restaurant.menuservice.entity.MenuItem;
import com.restaurant.menuservice.enums.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // =========================
    // FIND BY NAME
    // =========================

    Optional<MenuItem> findByNameIgnoreCaseAndRestaurantId(
            String name,
            Long restaurantId
    );

    // =========================
    // CHECK NAME EXISTS
    // =========================

    boolean existsByNameIgnoreCaseAndRestaurantId(
            String name,
            Long restaurantId
    );

    // =========================
    // FIND BY CATEGORY
    // =========================

    List<MenuItem> findByCategoryAndRestaurantId(
            MenuCategory category,
            Long restaurantId
    );

    // =========================
    // FIND BY AVAILABILITY
    // =========================

    List<MenuItem> findByAvailableAndRestaurantId(
            Boolean available,
            Long restaurantId
    );

    // =========================
    // CATEGORY + AVAILABILITY
    // =========================

    List<MenuItem> findByCategoryAndAvailableAndRestaurantId(
            MenuCategory category,
            Boolean available,
            Long restaurantId
    );

    // =========================
    // SEARCH BY NAME
    // =========================

    @Query("""
            SELECT m
            FROM MenuItem m
            WHERE m.restaurantId = :restaurantId
            AND LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<MenuItem> searchByName(
            @Param("keyword") String keyword,
            @Param("restaurantId") Long restaurantId
    );

    // =========================
    // AVAILABLE MENU
    // =========================

    @Query("""
            SELECT m
            FROM MenuItem m
            WHERE m.restaurantId = :restaurantId
            AND m.available = true
            ORDER BY m.category, m.name
            """)
    List<MenuItem> findAllAvailableOrderByCategoryAndName(
            @Param("restaurantId") Long restaurantId
    );

    // =========================
    // FIND ONE MENU ITEM
    // =========================

    Optional<MenuItem> findByIdAndRestaurantId(
            Long id,
            Long restaurantId
    );

    // =========================
    // FIND COMPLETE RESTAURANT MENU
    // =========================

    List<MenuItem> findAllByRestaurantId(
            Long restaurantId
    );

    // =========================
    // FIND AVAILABLE ITEMS
    // =========================

    List<MenuItem> findAllByRestaurantIdAndAvailableTrue(
            Long restaurantId
    );
}