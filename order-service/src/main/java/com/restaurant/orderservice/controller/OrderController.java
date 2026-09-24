package com.restaurant.orderservice.controller;

import com.restaurant.orderservice.dto.request.CreateOrderRequest;
import com.restaurant.orderservice.dto.request.UpdateOrderRequest;
import com.restaurant.orderservice.dto.response.ApiResponse;
import com.restaurant.orderservice.dto.response.OrderResponse;
import com.restaurant.orderservice.enums.OrderStatus;
import com.restaurant.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;


// ============================================================
// CREATE ORDER
// ============================================================

    /**
     * POST /api/orders
     * <p>
     * Create a new order.
     * <p>
     * Current user and restaurantId are obtained
     * from JwtUserDetails inside the service layer.
     */
    @PostMapping
    @PreAuthorize("hasRole('WAITER')")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {

        log.info("POST /api/orders - Creating new order");

        OrderResponse response =
                orderService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.created(
                                "Order created successfully",
                                response
                        )
                );
    }


// ============================================================
// UPDATE ORDER
// ============================================================

    /**
     * PUT /api/orders/{id}
     * <p>
     * Update an existing order.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderRequest request) {

        log.info(
                "PUT /api/orders/{} - Updating order",
                id
        );

        OrderResponse response =
                orderService.updateOrder(
                        id,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order updated successfully",
                        response
                )
        );
    }


// ============================================================
// CANCEL ORDER
// ============================================================

    /**
     * DELETE /api/orders/{id}
     * <p>
     * Cancel an existing order.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long id) {

        log.info(
                "DELETE /api/orders/{} - Cancelling order",
                id
        );

        orderService.cancelOrder(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order cancelled successfully"
                )
        );
    }


// ============================================================
// GET ORDER BY ID
// ============================================================

    /**
     * GET /api/orders/{id}
     * <p>
     * Get order by ID for the current restaurant.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id) {

        log.debug(
                "GET /api/orders/{}",
                id
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order retrieved",
                        orderService.getOrderById(id)
                )
        );
    }


// ============================================================
// GET ALL ORDERS
// ============================================================

    /**
     * GET /api/orders
     * <p>
     * Get all orders belonging to the current restaurant.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {

        log.debug("GET /api/orders");

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Orders retrieved",
                        orderService.getAllOrders()
                )
        );
    }


// ============================================================
// GET ORDERS BY STATUS
// ============================================================

    /**
     * GET /api/orders/status/{status}
     * <p>
     * Get orders by status for the current restaurant.
     */
    @GetMapping("/status/{status:(PENDING|PREPARING|READY|SERVED|CANCELLED)}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(
            @PathVariable OrderStatus status) {

        log.debug(
                "GET /api/orders/status/{}",
                status
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Orders retrieved by status",
                        orderService.getOrdersByStatus(status)
                )
        );
    }


// ============================================================
// GET ORDERS BY TABLE
// ============================================================

    /**
     * GET /api/orders/table/{tableNumber}
     * <p>
     * Get orders for a specific table
     * belonging to the current restaurant.
     */
    @GetMapping("/table/{tableNumber:\\d+}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByTable(
            @PathVariable Integer tableNumber) {

        log.debug(
                "GET /api/orders/table/{}",
                tableNumber
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Orders retrieved for table",
                        orderService.getOrdersByTableNumber(
                                tableNumber
                        )
                )
        );
    }
}
