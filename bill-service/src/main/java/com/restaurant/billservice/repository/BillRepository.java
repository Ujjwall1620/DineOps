package com.restaurant.billservice.repository;

import com.restaurant.billservice.entity.Bill;
import com.restaurant.billservice.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    Optional<Bill> findByBillNumber(String billNumber);

    Optional<Bill> findByIdempotencyKey(String idempotencyKey);

    List<Bill> findByStatus(BillStatus status);

    List<Bill> findByWaiterId(Long waiterId);

    List<Bill> findByTableNumber(Integer tableNumber);

    // Stats
    long countByStatus(BillStatus status);

    @Query("SELECT b FROM Bill b WHERE b.status NOT IN ('PAID','CANCELLED','REFUNDED') ORDER BY b.createdAt ASC")
    List<Bill> findAllPendingPaymentBills();
}
