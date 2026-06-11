package com.restaurant.menuservice.repository;

import com.restaurant.menuservice.entity.MenuItem;
import com.restaurant.menuservice.enums.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    Optional<MenuItem> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<MenuItem> findByCategory(MenuCategory category);

    List<MenuItem> findByAvailable(Boolean available);

    List<MenuItem> findByCategoryAndAvailable(MenuCategory category, Boolean available);

    /**
     * Case-insensitive partial-match search on name.
     */
    @Query("SELECT m FROM MenuItem m WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<MenuItem> searchByName(@Param("keyword") String keyword);

    /**
     * Available items for customer-facing menus.
     */
    @Query("SELECT m FROM MenuItem m WHERE m.available = true ORDER BY m.category, m.name")
    List<MenuItem> findAllAvailableOrderByCategoryAndName();
}
