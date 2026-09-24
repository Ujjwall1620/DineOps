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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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


    // ============================================================
    // CURRENT USER
    // ============================================================

    private JwtUserDetails getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return (JwtUserDetails) authentication.getPrincipal();
    }


    // ============================================================
    // CREATE ORDER
    // ============================================================

    @Transactional
    @Override
    public OrderResponse createOrder(
            CreateOrderRequest request
    ) {

        JwtUserDetails user = getCurrentUser();

        Long restaurantId = user.getRestaurantId();

        log.info(
                "Creating order for waiter: {} at table: {} for restaurantId: {}",
                user.getUsername(),
                request.getTableNumber(),
                restaurantId
        );

        Order order = Order.builder()
                .orderNumber(orderNumberGenerator.generate())
                .tableNumber(request.getTableNumber())
                .waiterId(user.getUserId())
                .waiterEmail(user.getUsername())
                .restaurantId(restaurantId)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        List<OrderItem> resolvedItems =
                resolveAndValidateItems(
                        request.getItems(),
                        order
                );

        resolvedItems.forEach(order::addItem);

        order.recalculateTotal();

        Order savedOrder =
                orderRepository.save(order);

        log.info(
                "Order created successfully: {} for restaurantId: {}",
                savedOrder.getOrderNumber(),
                restaurantId
        );

        orderProducer.publishOrderCreated(savedOrder);

        return orderMapper.toOrderResponse(savedOrder);
    }


    // ============================================================
    // UPDATE ORDER
    // ============================================================

    @Transactional
    @Override
    public OrderResponse updateOrder(
            Long orderId,
            UpdateOrderRequest request
    ) {

        JwtUserDetails user = getCurrentUser();

        Long restaurantId = user.getRestaurantId();

        log.info(
                "Updating order id: {} by user: {} for restaurantId: {}",
                orderId,
                user.getUsername(),
                restaurantId
        );

        Order order =
                findOrderById(
                        orderId,
                        restaurantId
                );

        order.getItems().clear();

        List<OrderItem> resolvedItems =
                resolveAndValidateItems(
                        request.getItems(),
                        order
                );

        resolvedItems.forEach(order::addItem);

        order.recalculateTotal();

        Order savedOrder =
                orderRepository.save(order);

        orderProducer.publishOrderUpdated(savedOrder);

        return orderMapper.toOrderResponse(savedOrder);
    }


    // ============================================================
    // CANCEL ORDER
    // ============================================================

    @Transactional
    @Override
    public void cancelOrder(Long orderId) {

        JwtUserDetails user = getCurrentUser();

        Long restaurantId = user.getRestaurantId();

        log.info(
                "Cancelling order id: {} by user: {} for restaurantId: {}",
                orderId,
                user.getUsername(),
                restaurantId
        );

        Order order =
                findOrderById(
                        orderId,
                        restaurantId
                );

        if (order.getStatus() == OrderStatus.SERVED) {

            throw new OrderCancellationException(
                    "Cannot cancel order ["
                            + order.getOrderNumber()
                            + "]. Order has already been served."
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {

            throw new OrderCancellationException(
                    "Order ["
                            + order.getOrderNumber()
                            + "] is already cancelled."
            );
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder =
                orderRepository.save(order);

        log.info(
                "Order cancelled successfully: {} for restaurantId: {}",
                savedOrder.getOrderNumber(),
                restaurantId
        );

        orderProducer.publishOrderCancelled(savedOrder);
    }


    // ============================================================
    // GET ORDER BY ID
    // ============================================================

    @Transactional(readOnly = true)
    @Override
    public OrderResponse getOrderById(Long orderId) {

        Long restaurantId =
                getCurrentUser().getRestaurantId();

        Order order =
                findOrderById(
                        orderId,
                        restaurantId
                );

        return orderMapper.toOrderResponse(order);
    }


    // ============================================================
    // GET ALL ORDERS
    // ============================================================

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponse> getAllOrders() {

        Long restaurantId =
                getCurrentUser().getRestaurantId();

        return orderMapper.toOrderResponseList(
                orderRepository.findAllByRestaurantId(
                        restaurantId
                )
        );
    }


    // ============================================================
    // GET ORDERS BY STATUS
    // ============================================================

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponse> getOrdersByStatus(
            OrderStatus status
    ) {

        Long restaurantId =
                getCurrentUser().getRestaurantId();

        return orderMapper.toOrderResponseList(
                orderRepository.findByStatusAndRestaurantId(
                        status,
                        restaurantId
                )
        );
    }


    // ============================================================
    // GET ORDERS BY TABLE NUMBER
    // ============================================================

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponse> getOrdersByTableNumber(
            Integer tableNumber
    ) {

        Long restaurantId =
                getCurrentUser().getRestaurantId();

        return orderMapper.toOrderResponseList(
                orderRepository.findByTableNumberAndRestaurantId(
                        tableNumber,
                        restaurantId
                )
        );
    }


    // ============================================================
    // FIND ORDER BY ID
    // ============================================================

    private Order findOrderById(
            Long orderId,
            Long restaurantId
    ) {

        return orderRepository
                .findByIdAndRestaurantId(
                        orderId,
                        restaurantId
                )
                .orElseThrow(
                        () -> new OrderNotFoundException(orderId)
                );
    }


    // ============================================================
    // RESOLVE AND VALIDATE ITEMS
    // ============================================================

    private List<OrderItem> resolveAndValidateItems(
            List<OrderItemRequest> itemRequests,
            Order order
    ) {

        Long restaurantId = order.getRestaurantId();

        Map<Long, Integer> mergedQuantities =
                itemRequests.stream()
                        .collect(Collectors.toMap(
                                OrderItemRequest::getMenuItemId,
                                OrderItemRequest::getQuantity,
                                Integer::sum
                        ));

        List<OrderItem> resolvedItems =
                new ArrayList<>();

        for (Map.Entry<Long, Integer> entry
                : mergedQuantities.entrySet()) {

            Long menuItemId = entry.getKey();

            Integer requestedQty = entry.getValue();

            MenuItemResponse menuItem =
                    fetchMenuItemOrThrow(menuItemId);

            if (!menuItem.getRestaurantId().equals(restaurantId)) {

                throw new MenuItemNotFoundException(
                        menuItemId
                );
            }

            if (menuItem.getAvailableStock() < requestedQty) {

                throw new OutOfStockException(
                        menuItem.getName(),
                        requestedQty,
                        menuItem.getAvailableStock()
                );
            }

            BigDecimal subtotal =
                    menuItem.getPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            requestedQty
                                    )
                            );

            OrderItem item =
                    OrderItem.builder()
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


    // ============================================================
    // FETCH MENU ITEM
    // ============================================================

    private MenuItemResponse fetchMenuItemOrThrow(
            Long menuItemId
    ) {

        try {

            MenuItemResponse menuItem =
                    menuClient.getMenuItemById(
                            menuItemId
                    );

            if (menuItem == null) {

                log.warn(
                        "Menu item returned null from client for id: {}",
                        menuItemId
                );

                throw new MenuItemNotFoundException(
                        menuItemId
                );
            }

            if (menuItem.getRestaurantId() == null) {

                log.error(
                        "Menu item {} has null restaurantId",
                        menuItemId
                );

                throw new MenuItemNotFoundException(
                        menuItemId
                );
            }

            return menuItem;

        } catch (FeignException.NotFound e) {

            log.warn(
                    "Menu item not found via Feign for id: {}",
                    menuItemId
            );

            throw new MenuItemNotFoundException(
                    menuItemId
            );

        } catch (FeignException e) {

            log.error(
                    "Feign error fetching menu item {}: {}",
                    menuItemId,
                    e.getMessage()
            );

            throw new RuntimeException(
                    "Failed to communicate with Menu Service: "
                            + e.getMessage()
            );
        }
    }
}

