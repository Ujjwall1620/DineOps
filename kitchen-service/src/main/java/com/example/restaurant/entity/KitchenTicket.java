package com.example.restaurant.entity;

import com.example.restaurant.enums.KitchenStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "kitchen_tickets",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_restaurant_order",
                columnNames = {"restaurant_id", "order_id"}
        )
)
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KitchenTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

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

    @OneToMany(mappedBy = "kitchenTicket", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<KitchenItem> items = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    public void addItem(KitchenItem item) {
        items.add(item);
        item.setKitchenTicket(this);
    }

    public void replaceItems(List<KitchenItem> newItems) {
        this.items.clear();
        newItems.forEach(this::addItem);
    }
}