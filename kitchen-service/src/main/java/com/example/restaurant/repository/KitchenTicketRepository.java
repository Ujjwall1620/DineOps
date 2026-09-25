package com.example.restaurant.repository;

import com.example.restaurant.entity.KitchenTicket;
import com.example.restaurant.enums.KitchenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, Long> {

    Optional<KitchenTicket> findByRestaurantIdAndOrderId(Long restaurantId, Long orderId);

    boolean existsByRestaurantIdAndOrderId(Long restaurantId, Long orderId);

    List<KitchenTicket> findByRestaurantIdAndOrderIdIn(Long restaurantId, List<Long> orderIds);

    List<KitchenTicket> findByRestaurantIdAndStatusOrderByCreatedAtAsc(Long restaurantId, KitchenStatus status);

    // ✅ NEW — ye sabse important hai: ticketId + restaurantId dono match hone chahiye
    // Warna Restaurant A, Restaurant B ka ticket ID guess/reuse karke uska data touch kar sakta hai
    Optional<KitchenTicket> findByIdAndRestaurantId(Long id, Long restaurantId);

    // ✅ NEW — pehle jo global tha, ab restaurant-scoped
    @Query("""
        SELECT t FROM KitchenTicket t
        WHERE t.restaurantId = :restaurantId
        AND t.status NOT IN ('COMPLETED','CANCELLED')
        ORDER BY t.createdAt ASC
    """)
    List<KitchenTicket> findAllActiveOrderByCreatedAtAsc(@Param("restaurantId") Long restaurantId);

    // ✅ NEW — stats bhi restaurant-scoped
    long countByRestaurantIdAndStatus(Long restaurantId, KitchenStatus status);
}