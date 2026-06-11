package com.restaurant.orderservice.service;

import com.restaurant.orderservice.dto.response.OrderItemResponse;
import com.restaurant.orderservice.dto.response.OrderResponse;
import com.restaurant.orderservice.entity.Order;
import com.restaurant.orderservice.entity.OrderItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .tableNumber(order.getTableNumber())
                .waiterId(order.getWaiterId())
                .waiterEmail(order.getWaiterEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(toOrderItemResponseList(order.getItems()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public List<OrderResponse> toOrderResponseList(List<Order> orders) {
        return orders.stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItemId())
                .menuItemName(item.getMenuItemName())
                .quantity(item.getQuantity())
                .pricePerUnit(item.getPricePerUnit())
                .subtotal(item.getSubtotal())
                .build();
    }

    public List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> items) {
        return items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }
}
