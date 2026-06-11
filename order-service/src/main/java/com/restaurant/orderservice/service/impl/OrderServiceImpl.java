package com.restaurant.orderservice.service.impl;

import com.restaurant.orderservice.client.MenuClient;
import com.restaurant.orderservice.dto.request.CreateOrderRequest;
import com.restaurant.orderservice.dto.request.OrderItemRequest;
import com.restaurant.orderservice.dto.request.UpdateOrderRequest;
import com.restaurant.orderservice.dto.response.MenuItemResponse;
import com.restaurant.orderservice.dto.response.OrderResponse;
import com.restaurant.orderservice.entity.Order;
import com.restaurant.orderservice.entity.OrderItem;
import com.restaurant.orderservice.enums.OrderStatus;
import com.restaurant.orderservice.exception.MenuItemNotFoundException;
import com.restaurant.orderservice.exception.OrderCancellationException;
import com.restaurant.orderservice.exception.OrderNotFoundException;
import com.restaurant.orderservice.exception.OutOfStockException;
import com.restaurant.orderservice.kafka.OrderProducer;
import com.restaurant.orderservice.repository.OrderRepository;
import com.restaurant.orderservice.security.JwtUserDetails;
import com.restaurant.orderservice.service.OrderMapper;
import com.restaurant.orderservice.service.OrderNumberGenerator;
import com.restaurant.orderservice.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final MenuClient menuClient;
    private final OrderProducer orderProducer;
    private final OrderMapper orderMapper;
    private final OrderNumberGenerator orderNumberGenerator;

    // ─── Create Order ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, JwtUserDetails waiter) {
        log.info("Creating order for waiter: {} at table: {}", waiter.getUsername(), request.getTableNumber());

        // Build the Order shell
        Order order = Order.builder()
                .orderNumber(orderNumberGenerator.generate())
                .tableNumber(request.getTableNumber())
                .waiterId(waiter.getUserId())
                .waiterEmail(waiter.getUsername())
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        // Resolve and validate items (merges duplicates)
        List<OrderItem> resolvedItems = resolveAndValidateItems(request.getItems(), order);
        resolvedItems.forEach(order::addItem);
        order.recalculateTotal();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully: {}", savedOrder.getOrderNumber());

        // Publish Kafka event (fire-and-forget; not part of transaction)
        orderProducer.publishOrderCreated(savedOrder);

        return orderMapper.toOrderResponse(savedOrder);
    }

    // ─── Update Order ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public OrderResponse updateOrder(Long orderId, UpdateOrderRequest request, JwtUserDetails waiter) {
        log.info("Updating order id: {} by waiter: {}", orderId, waiter.getUsername());

        Order order = findOrderById(orderId);

        // Clear existing items; they are orphaned and deleted by JPA
        order.getItems().clear();

        // Resolve new item list (also handles merging duplicates)
        List<OrderItem> resolvedItems = resolveAndValidateItems(request.getItems(), order);
        resolvedItems.forEach(order::addItem);
        order.recalculateTotal();

        Order savedOrder = orderRepository.save(order);
        log.info("Order updated successfully: {}", savedOrder.getOrderNumber());

        orderProducer.publishOrderUpdated(savedOrder);

        return orderMapper.toOrderResponse(savedOrder);
    }

    // ─── Cancel Order ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void cancelOrder(Long orderId, JwtUserDetails waiter) {
        log.info("Cancelling order id: {} by waiter: {}", orderId, waiter.getUsername());

        Order order = findOrderById(orderId);

        if (order.getStatus() == OrderStatus.SERVED) {
            throw new OrderCancellationException(
                    "Cannot cancel order [" + order.getOrderNumber() + "]. Order has already been served.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new OrderCancellationException(
                    "Order [" + order.getOrderNumber() + "] is already cancelled.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        log.info("Order cancelled successfully: {}", savedOrder.getOrderNumber());

        orderProducer.publishOrderCancelled(savedOrder);
    }

    // ─── Read Operations ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        return orderMapper.toOrderResponse(findOrderById(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderMapper.toOrderResponseList(orderRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderMapper.toOrderResponseList(orderRepository.findByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByTableNumber(Integer tableNumber) {
        return orderMapper.toOrderResponseList(orderRepository.findByTableNumber(tableNumber));
    }

    // ─── Internal Helpers ──────────────────────────────────────────────────────

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    /**
     * Resolves a list of OrderItemRequests into validated OrderItem entities.
     * <p>
     * Business rules applied:
     * 1. Merge duplicate menu item IDs by summing quantities.
     * 2. Fetch each menu item from the Menu Service.
     * 3. Validate stock availability.
     * 4. Compute subtotal from backend price (never from request).
     */
    private List<OrderItem> resolveAndValidateItems(List<OrderItemRequest> itemRequests, Order order) {
        // Step 1: Merge duplicates
        Map<Long, Integer> mergedQuantities = itemRequests.stream()
                .collect(Collectors.toMap(
                        OrderItemRequest::getMenuItemId,
                        OrderItemRequest::getQuantity,
                        Integer::sum
                ));

        List<OrderItem> resolvedItems = new ArrayList<>();

        for (Map.Entry<Long, Integer> entry : mergedQuantities.entrySet()) {
            Long menuItemId = entry.getKey();
            Integer requestedQty = entry.getValue();

            // Step 2: Fetch from Menu Service
            MenuItemResponse menuItem = fetchMenuItemOrThrow(menuItemId);

            // Step 3: Stock validation
            if (menuItem.getAvailableStock() < requestedQty) {
                throw new OutOfStockException(
                        menuItem.getName(), requestedQty, menuItem.getAvailableStock());
            }

            // Step 4: Build item with backend price
            BigDecimal subtotal = menuItem.getPrice()
                    .multiply(BigDecimal.valueOf(requestedQty));

            OrderItem item = OrderItem.builder()
                    .menuItemId(menuItem.getId())
                    .menuItemName(menuItem.getName())
                    .quantity(requestedQty)
                    .pricePerUnit(menuItem.getPrice())
                    .subtotal(subtotal)
                    .build();

            resolvedItems.add(item);
        }

        return resolvedItems;
    }

    private MenuItemResponse fetchMenuItemOrThrow(Long menuItemId) {
        try {
            MenuItemResponse menuItem = menuClient.getMenuItemById(menuItemId);
            if (menuItem == null) {
                throw new MenuItemNotFoundException(menuItemId);
            }
            return menuItem;
        } catch (FeignException.NotFound e) {
            log.warn("Menu item not found via Feign for id: {}", menuItemId);
            throw new MenuItemNotFoundException(menuItemId);
        } catch (FeignException e) {
            log.error("Feign error fetching menu item {}: {}", menuItemId, e.getMessage());
            throw new RuntimeException("Failed to communicate with Menu Service: " + e.getMessage());
        }
    }
}
