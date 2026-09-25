package com.example.restaurant.service.impl;

import com.example.restaurant.Security.JwtUserDetails;
import com.example.restaurant.dto.request.AssignChefRequest;
import com.example.restaurant.dto.response.KitchenStatsResponse;
import com.example.restaurant.dto.response.KitchenTicketResponse;
import com.example.restaurant.entity.KitchenTicket;
import com.example.restaurant.enums.KitchenStatus;
import com.example.restaurant.exception.InvalidStatusTransitionException;
import com.example.restaurant.exception.KitchenTicketNotFoundException;
import com.example.restaurant.kafka.producer.KitchenProducer;
import com.example.restaurant.repository.KitchenTicketRepository;
import com.example.restaurant.service.KitchenService;
import com.example.restaurant.service.KitchenTicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenServiceImpl implements KitchenService {

    private final KitchenTicketRepository ticketRepository;
    private final KitchenTicketMapper     mapper;
    private final KitchenProducer         kitchenProducer;

    // ─── Chef Workflow ─────────────────────────────────────────────────────────

    /**
     * Chef starts cooking.
     * Valid transition: PENDING → IN_PREPARATION
     * Publishes ORDER_COOKING_STARTED event.
     */


    private JwtUserDetails getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        return (JwtUserDetails) authentication.getPrincipal();
    }


    @Override
    @Transactional
    public KitchenTicketResponse startPreparation(Long ticketId) {
        log.info("Chef starting preparation for ticket id: {}", ticketId);

        KitchenTicket ticket = findById(ticketId);
        validateTransition(ticket.getStatus(), KitchenStatus.IN_PREPARATION);

        ticket.setStatus(KitchenStatus.IN_PREPARATION);
        KitchenTicket saved = ticketRepository.save(ticket);

        kitchenProducer.publishCookingStarted(saved);
        log.info("Ticket {} moved to IN_PREPARATION", ticketId);

        return mapper.toResponse(saved);
    }

    /**
     * Chef marks food ready for pickup.
     * Valid transition: IN_PREPARATION → READY
     * Publishes ORDER_READY event.
     */
    @Override
    @Transactional
    public KitchenTicketResponse markReady(Long ticketId) {
        log.info("Chef marking ticket ready, id: {}", ticketId);

        KitchenTicket ticket = findById(ticketId);
        validateTransition(ticket.getStatus(), KitchenStatus.READY);

        ticket.setStatus(KitchenStatus.READY);
        KitchenTicket saved = ticketRepository.save(ticket);

        kitchenProducer.publishOrderReady(saved);
        log.info("Ticket {} moved to READY", ticketId);

        return mapper.toResponse(saved);
    }

    /**
     * Assign a chef to a ticket.
     * Chef can be assigned in any non-terminal state.
     */
    @Override
    @Transactional
    public KitchenTicketResponse assignChef(Long ticketId, AssignChefRequest request) {
        log.info("Assigning chef [{}] {} to ticket id: {}",
                request.getChefId(), request.getChefName(), ticketId);

        KitchenTicket ticket = findById(ticketId);

        if (isTerminal(ticket.getStatus())) {
            throw new InvalidStatusTransitionException(ticket.getStatus(), ticket.getStatus()); // terminal guard
        }

        ticket.setChefId(request.getChefId());
        ticket.setChefName(request.getChefName());
        KitchenTicket saved = ticketRepository.save(ticket);

        log.info("Chef {} assigned to ticket {}", request.getChefName(), ticketId);
        return mapper.toResponse(saved);
    }

    // ─── Read Operations ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public KitchenTicketResponse getTicketById(Long ticketId) {
        return mapper.toResponse(findById(ticketId));
    }

    @Override
    @Transactional(readOnly = true)
    public KitchenTicketResponse getTicketByOrderId(Long orderId) {
        Long restaurantId = getCurrentUser().getRestaurantId();
        KitchenTicket ticket = ticketRepository.findByRestaurantIdAndOrderId(restaurantId, orderId)
                .orElseThrow(() -> new KitchenTicketNotFoundException(
                        "Kitchen ticket not found for orderId: " + orderId));
        return mapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTicketResponse> getPendingTickets() {
        Long restaurantId = getCurrentUser().getRestaurantId();
        return mapper.toResponseList(
                ticketRepository.findByRestaurantIdAndStatusOrderByCreatedAtAsc(restaurantId, KitchenStatus.PENDING));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTicketResponse> getTicketsByStatus(KitchenStatus status) {
        Long restaurantId = getCurrentUser().getRestaurantId();
        return mapper.toResponseList(
                ticketRepository.findByRestaurantIdAndStatusOrderByCreatedAtAsc(restaurantId, status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTicketResponse> getActiveTickets() {
        Long restaurantId = getCurrentUser().getRestaurantId();
        return mapper.toResponseList(
                ticketRepository.findAllActiveOrderByCreatedAtAsc(restaurantId));
    }

    // ─── Dashboard ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public KitchenStatsResponse getStats() {
        Long restaurantId = getCurrentUser().getRestaurantId();
        long pending     = ticketRepository.countByRestaurantIdAndStatus(restaurantId, KitchenStatus.PENDING);
        long preparing   = ticketRepository.countByRestaurantIdAndStatus(restaurantId, KitchenStatus.IN_PREPARATION);
        long ready       = ticketRepository.countByRestaurantIdAndStatus(restaurantId, KitchenStatus.READY);
        long completed   = ticketRepository.countByRestaurantIdAndStatus(restaurantId, KitchenStatus.COMPLETED);
        long cancelled   = ticketRepository.countByRestaurantIdAndStatus(restaurantId, KitchenStatus.CANCELLED);

        return KitchenStatsResponse.builder()
                .pendingOrders(pending)
                .preparingOrders(preparing)
                .readyOrders(ready)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .totalActiveOrders(pending + preparing + ready)
                .build();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

    private KitchenTicket findById(Long id) {
        Long restaurantId = getCurrentUser().getRestaurantId();
        return ticketRepository.findByIdAndRestaurantId(id, restaurantId)
                .orElseThrow(() -> new KitchenTicketNotFoundException(id));
    }

    /**
     * State machine guard.
     * Allowed transitions:
     *   PENDING       → IN_PREPARATION
     *   IN_PREPARATION → READY
     *   READY         → COMPLETED  (future use)
     *   Any           → CANCELLED  (future use)
     */
    private void validateTransition(KitchenStatus from, KitchenStatus to) {
        boolean valid = switch (to) {
            case IN_PREPARATION -> from == KitchenStatus.PENDING;
            case READY          -> from == KitchenStatus.IN_PREPARATION;
            case COMPLETED      -> from == KitchenStatus.READY;
            case CANCELLED      -> from != KitchenStatus.COMPLETED && from != KitchenStatus.CANCELLED;
            default             -> false;
        };

        if (!valid) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }

    private boolean isTerminal(KitchenStatus status) {
        return status == KitchenStatus.COMPLETED || status == KitchenStatus.CANCELLED;
    }
}
