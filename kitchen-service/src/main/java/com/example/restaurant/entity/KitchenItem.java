package com.restaurant.kitchenservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "kitchen_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KitchenItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kitchen_ticket_id", nullable = false)
    private KitchenTicket kitchenTicket;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "menu_item_name", nullable = false, length = 150)
    private String menuItemName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
