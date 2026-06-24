package com.restaurant.billservice.repository;

import com.restaurant.billservice.entity.PaymentTransaction;
import com.restaurant.billservice.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    List<PaymentTransaction> findByBillIdOrderByCreatedAtDesc(Long billId);

    Optional<PaymentTransaction> findByTransactionRef(String transactionRef);

    Optional<PaymentTransaction> findByGatewayTransactionId(String gatewayTransactionId);

    List<PaymentTransaction> findByStatus(TransactionStatus status);
}
