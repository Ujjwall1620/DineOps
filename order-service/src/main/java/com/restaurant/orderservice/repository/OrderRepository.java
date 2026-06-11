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

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByTableNumber(Integer tableNumber);

    List<Order> findByWaiterId(Long waiterId);

    @Query("SELECT o FROM Order o WHERE o.tableNumber = :tableNumber AND o.status NOT IN ('SERVED', 'CANCELLED')")
    List<Order> findActiveOrdersByTable(@Param("tableNumber") Integer tableNumber);

    boolean existsByOrderNumber(String orderNumber);

    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE")
    long countOrdersCreatedToday();
}
