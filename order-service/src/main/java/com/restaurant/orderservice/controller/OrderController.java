package com.restaurant.orderservice.controller;

import com.restaurant.orderservice.dto.request.CreateOrderRequest;
import com.restaurant.orderservice.dto.request.UpdateOrderRequest;
import com.restaurant.orderservice.dto.response.ApiResponse;
import com.restaurant.orderservice.dto.response.OrderResponse;
import com.restaurant.orderservice.enums.OrderStatus;
import com.restaurant.orderservice.security.JwtUserDetails;
import com.restaurant.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders
     * Create a new order. Only WAITER role allowed.
     * Waiter details are extracted from JWT — not from the request body.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal JwtUserDetails waiter) {

        log.info("POST /api/orders - waiter: {}", waiter.getUsername());
        OrderResponse response = orderService.createOrder(request, waiter);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Order created successfully", response));
    }

    /**
     * PUT /api/orders/{id}
     * Update an existing order. Only WAITER role allowed.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request,
            @AuthenticationPrincipal JwtUserDetails waiter) {

        log.info("PUT /api/orders/{} - waiter: {}", id, waiter.getUsername());
        OrderResponse response = orderService.updateOrder(id, request, waiter);
        return ResponseEntity.ok(ApiResponse.success("Order updated successfully", response));
    }

    /**
     * DELETE /api/orders/{id}
     * Cancel an order. Only WAITER role allowed.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal JwtUserDetails waiter) {

        log.info("DELETE /api/orders/{} - waiter: {}", id, waiter.getUsername());
        orderService.cancelOrder(id, waiter);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully"));
    }

    /**
     * GET /api/orders/{id}
     * Get order by ID. Any authenticated user can view.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        log.debug("GET /api/orders/{}", id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved",
                orderService.getOrderById(id)));
    }

    /**
     * GET /api/orders
     * Get all orders.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        log.debug("GET /api/orders");
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved",
                orderService.getAllOrders()));
    }

    /**
     * GET /api/orders/status/{status}
     * Get orders filtered by status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            @PathVariable OrderStatus status) {
        log.debug("GET /api/orders/status/{}", status);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved by status",
                orderService.getOrdersByStatus(status)));
    }

    /**
     * GET /api/orders/table/{tableNumber}
     * Get orders for a specific table.
     */
    @GetMapping("/table/{tableNumber}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByTable(
            @PathVariable Integer tableNumber) {
        log.debug("GET /api/orders/table/{}", tableNumber);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved for table",
                orderService.getOrdersByTableNumber(tableNumber)));
    }
}
