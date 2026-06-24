package com.restaurant.billservice.entity;

import com.restaurant.billservice.enums.BillStatus;
import com.restaurant.billservice.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bills")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_number", nullable = false, unique = true, length = 50)
    private String billNumber;

    /**
     * orderId from Order Service — unique, not a FK (separate DB).
     * Idempotency guard: one bill per order, always.
     */
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "table_number", nullable = false)
    private Integer tableNumber;

    @Column(name = "waiter_id", nullable = false)
    private Long waiterId;

    @Column(name = "waiter_name", nullable = false, length = 100)
    private String waiterName;

    // ─── Financial fields ──────────────────────────────────────────────────────

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "gst_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal gstPercentage;

    @Column(name = "gst_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal gstAmount;

    @Column(name = "service_charge_pct", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal serviceChargePct = BigDecimal.ZERO;

    @Column(name = "service_charge_amt", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal serviceChargeAmt = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal grandTotal;

    // ─── Status and payment ────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BillStatus status = BillStatus.GENERATED;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 30)
    private PaymentMethod paymentMethod;

    /**
     * Client-supplied idempotency key for payment requests.
     * Prevents double-charging on retries.
     */
    @Column(name = "idempotency_key", length = 100, unique = true)
    private String idempotencyKey;

    /** Razorpay short URL returned when payment link is created. */
    @Column(name = "payment_url", length = 500)
    private String paymentUrl;

    /**
     * Optimistic locking — prevents concurrent payment attempts
     * from both succeeding on the same bill.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    // ─── Relationships ─────────────────────────────────────────────────────────

    @OneToMany(mappedBy = "bill",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<BillItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "bill",
               cascade = CascadeType.ALL,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<PaymentTransaction> transactions = new ArrayList<>();

    // ─── Audit ────────────────────────────────────────────────────────────────

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─── Convenience helpers — mirrors KitchenTicket.addItem() pattern ─────────

    public void addItem(BillItem item) {
        items.add(item);
        item.setBill(this);
    }

    public void addTransaction(PaymentTransaction transaction) {
        transactions.add(transaction);
        transaction.setBill(this);
    }
}
