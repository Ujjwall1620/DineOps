package com.restaurant.kitchenservice.entity;

import com.restaurant.kitchenservice.enums.KitchenStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "kitchen_tickets")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mirrors the originating orderId from Order Service.
     * Unique constraint prevents duplicate ticket creation for the same order.
     */
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "order_number", nullable = false, length = 50)
    private String orderNumber;

    @Column(name = "table_number", nullable = false)
    private Integer tableNumber;

    @Column(name = "chef_id")
    private Long chefId;

    @Column(name = "chef_name", length = 100)
    private String chefName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private KitchenStatus status = KitchenStatus.PENDING;

    @OneToMany(mappedBy = "kitchenTicket",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.LAZY)
    @Builder.Default
    private List<KitchenItem> items = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ─── Convenience helpers ───────────────────────────────────────────────────

    public void addItem(KitchenItem item) {
        items.add(item);
        item.setKitchenTicket(this);
    }
}
