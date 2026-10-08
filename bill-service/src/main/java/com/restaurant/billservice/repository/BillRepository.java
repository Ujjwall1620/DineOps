package com.restaurant.billservice.repository;

import com.restaurant.billservice.entity.Bill;
import com.restaurant.billservice.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    // ─────────────────────────────────────────────
    // BILL LOOKUPS
    // ─────────────────────────────────────────────

    Optional<Bill> findByRestaurantIdAndOrderId(
            Long restaurantId,
            Long orderId
    );

    boolean existsByRestaurantIdAndOrderId(
            Long restaurantId,
            Long orderId
    );

    Optional<Bill> findByRestaurantIdAndId(
            Long restaurantId,
            Long Id
    );

    Optional<Bill> findByRestaurantIdAndIdempotencyKey(
            Long restaurantId,
            String idempotencyKey
    );


    // ─────────────────────────────────────────────
    // STATUS
    // ─────────────────────────────────────────────

    List<Bill> findByRestaurantIdAndStatus(
            Long restaurantId,
            BillStatus status
    );


    // ─────────────────────────────────────────────
    // WAITER
    // ─────────────────────────────────────────────

    List<Bill> findByRestaurantIdAndWaiterId(
            Long restaurantId,
            Long waiterId
    );


    // ─────────────────────────────────────────────
    // TABLE
    // ─────────────────────────────────────────────

    List<Bill> findByRestaurantIdAndTableNumber(
            Long restaurantId,
            Integer tableNumber
    );


    // ─────────────────────────────────────────────
    // STATS
    // ─────────────────────────────────────────────

    long countByRestaurantIdAndStatus(
            Long restaurantId,
            BillStatus status
    );


    // ─────────────────────────────────────────────
    // PENDING PAYMENT BILLS
    // ─────────────────────────────────────────────

    @Query("""
            SELECT b
            FROM Bill b
            WHERE b.restaurantId = :restaurantId
              AND b.status NOT IN (
                    com.restaurant.billservice.enums.BillStatus.PAID,
                    com.restaurant.billservice.enums.BillStatus.CANCELLED,
                    com.restaurant.billservice.enums.BillStatus.REFUNDED
              )
            ORDER BY b.createdAt ASC
            """)
    List<Bill> findAllPendingPaymentBills(
            @Param("restaurantId") Long restaurantId
    );


    // ─────────────────────────────────────────────
    // BATCH / KAFKA
    // ─────────────────────────────────────────────

    List<Bill> findByRestaurantIdAndOrderIdIn(
            Long restaurantId,
            List<Long> orderIds
    );

    List<Bill> findByRestaurantId(Long restaurantId);
}
