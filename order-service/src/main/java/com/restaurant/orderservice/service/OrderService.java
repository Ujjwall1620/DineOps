package com.restaurant.orderservice.service;

import com.restaurant.orderservice.dto.request.CreateOrderRequest;
import com.restaurant.orderservice.dto.request.UpdateOrderRequest;
import com.restaurant.orderservice.dto.response.OrderResponse;
import com.restaurant.orderservice.enums.OrderStatus;
import com.restaurant.orderservice.security.JwtUserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OrderService {


    @Transactional
    OrderResponse createOrder(
            CreateOrderRequest request
    );

    @Transactional
    OrderResponse updateOrder(
            Long orderId,
            UpdateOrderRequest request
    );

    @Transactional
    void cancelOrder(Long orderId);

    @Transactional(readOnly = true)
    OrderResponse getOrderById(Long orderId);

    @Transactional(readOnly = true)
    List<OrderResponse> getAllOrders();

    @Transactional(readOnly = true)
    List<OrderResponse> getOrdersByStatus(
            OrderStatus status
    );

    @Transactional(readOnly = true)
    List<OrderResponse> getOrdersByTableNumber(
            Integer tableNumber
    );
}
