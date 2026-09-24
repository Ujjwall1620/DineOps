package com.restaurant.orderservice.repository;

import com.restaurant.orderservice.entity.Order;
import com.restaurant.orderservice.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

// ============================================================
// FIND ORDER BY ID + RESTAURANT
// ============================================================

    Optional<Order> findByIdAndRestaurantId(
            Long orderId,
            Long restaurantId
    );


// ============================================================
// FIND ALL ORDERS BY RESTAURANT
// ============================================================

    List<Order> findAllByRestaurantId(
            Long restaurantId
    );


// ============================================================
// FIND ORDER BY ORDER NUMBER + RESTAURANT
// ============================================================

    Optional<Order> findByOrderNumberAndRestaurantId(
            String orderNumber,
            Long restaurantId
    );


// ============================================================
// FIND ORDERS BY STATUS + RESTAURANT
// ============================================================

    List<Order> findByStatusAndRestaurantId(
            OrderStatus status,
            Long restaurantId
    );


// ============================================================
// FIND ORDERS BY TABLE + RESTAURANT
// ============================================================

    List<Order> findByTableNumberAndRestaurantId(
            Integer tableNumber,
            Long restaurantId
    );


// ============================================================
// FIND ORDERS BY WAITER + RESTAURANT
// ============================================================

    List<Order> findByWaiterIdAndRestaurantId(
            Long waiterId,
            Long restaurantId
    );


// ============================================================
// FIND ACTIVE ORDERS BY TABLE + RESTAURANT
// ============================================================

    @Query("""
       SELECT o FROM Order o
       WHERE o.tableNumber = :tableNumber
       AND o.restaurantId = :restaurantId
       AND o.status NOT IN ('SERVED', 'CANCELLED')
       """)
    List<Order> findActiveOrdersByTable(
            @Param("tableNumber") Integer tableNumber,
            @Param("restaurantId") Long restaurantId
    );


// ============================================================
// CHECK ORDER NUMBER EXISTS + RESTAURANT
// ============================================================

    boolean existsByOrderNumberAndRestaurantId(
            String orderNumber,
            Long restaurantId
    );


// ============================================================
// COUNT TODAY'S ORDERS + RESTAURANT
// ============================================================

    @Query("""
       SELECT COUNT(o)
       FROM Order o
       WHERE o.restaurantId = :restaurantId
       AND DATE(o.createdAt) = CURRENT_DATE
       """)
    long countOrdersCreatedToday(
            @Param("restaurantId") Long restaurantId
    );
}
