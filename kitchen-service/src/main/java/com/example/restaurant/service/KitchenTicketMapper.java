package com.example.restaurant.service;

import com.example.restaurant.dto.response.KitchenItemResponse;
import com.example.restaurant.dto.response.KitchenTicketResponse;
import com.example.restaurant.entity.KitchenItem;
import com.example.restaurant.entity.KitchenTicket;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class KitchenTicketMapper {

    public KitchenTicketResponse toResponse(KitchenTicket ticket) {
        return KitchenTicketResponse.builder()
                .id(ticket.getId())
                .orderId(ticket.getOrderId())
                .orderNumber(ticket.getOrderNumber())
                .tableNumber(ticket.getTableNumber())
                .chefId(ticket.getChefId())
                .chefName(ticket.getChefName())
                .status(ticket.getStatus())
                .items(toItemResponseList(ticket.getItems()))
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }

    public List<KitchenTicketResponse> toResponseList(List<KitchenTicket> tickets) {
        return tickets.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private KitchenItemResponse toItemResponse(KitchenItem item) {
        return KitchenItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .build();
    }

    private List<KitchenItemResponse> toItemResponseList(List<KitchenItem> items) {
        return items.stream().map(this::toItemResponse).collect(Collectors.toList());
    }
}
