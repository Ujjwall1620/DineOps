package com.restaurant.billservice.entity;

import com.restaurant.billservice.enums.PaymentMethod;
import com.restaurant.billservice.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Internal unique reference for every payment attempt.
     * Format: TXN-{billId}-{timestamp}
     */
    @Column(name = "transaction_ref", nullable = false, unique = true, length = 100)
    private String transactionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    /** Which gateway processed this attempt: dummy | razorpay | stripe */
    @Column(name = "gateway", nullable = false, length = 30)
    private String gateway;

    /** ID returned by the payment gateway on success. */
    @Column(name = "gateway_transaction_id", length = 200)
    private String gatewayTransactionId;

    /** Full raw response from gateway — stored for audit/dispute. */
    @Column(name = "gateway_response", columnDefinition = "TEXT")
    private String gatewayResponse;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
