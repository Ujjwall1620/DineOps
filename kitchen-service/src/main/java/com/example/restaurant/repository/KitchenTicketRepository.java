package com.restaurant.kitchenservice.repository;

import com.restaurant.kitchenservice.entity.KitchenTicket;
import com.restaurant.kitchenservice.enums.KitchenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, Long> {

    Optional<KitchenTicket> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    /** All tickets with the given status, oldest first — kitchen queue order. */
    List<KitchenTicket> findByStatusOrderByCreatedAtAsc(KitchenStatus status);

    /** All active (non-terminal) tickets, oldest first. */
    @Query("SELECT t FROM KitchenTicket t WHERE t.status NOT IN ('COMPLETED','CANCELLED') ORDER BY t.createdAt ASC")
    List<KitchenTicket> findAllActiveOrderByCreatedAtAsc();

    List<KitchenTicket> findByChefIdOrderByCreatedAtAsc(Long chefId);

    // ─── Stats counts ──────────────────────────────────────────────────────────
    long countByStatus(KitchenStatus status);
}
