package com.restaurant.orderservice.service;

import com.restaurant.orderservice.dto.request.CreateOrderRequest;
import com.restaurant.orderservice.dto.request.UpdateOrderRequest;
import com.restaurant.orderservice.dto.response.OrderResponse;
import com.restaurant.orderservice.enums.OrderStatus;
import com.restaurant.orderservice.security.JwtUserDetails;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request, JwtUserDetails waiter);

    OrderResponse updateOrder(Long orderId, UpdateOrderRequest request, JwtUserDetails waiter);

    void cancelOrder(Long orderId, JwtUserDetails waiter);

    OrderResponse getOrderById(Long orderId);

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByStatus(OrderStatus status);

    List<OrderResponse> getOrdersByTableNumber(Integer tableNumber);
}
