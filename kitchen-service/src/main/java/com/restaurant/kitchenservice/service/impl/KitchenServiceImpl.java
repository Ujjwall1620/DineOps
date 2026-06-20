package com.restaurant.kitchenservice.service.impl;

import com.restaurant.kitchenservice.dto.request.AssignChefRequest;
import com.restaurant.kitchenservice.dto.response.KitchenStatsResponse;
import com.restaurant.kitchenservice.dto.response.KitchenTicketResponse;
import com.restaurant.kitchenservice.entity.KitchenTicket;
import com.restaurant.kitchenservice.enums.KitchenStatus;
import com.restaurant.kitchenservice.exception.InvalidStatusTransitionException;
import com.restaurant.kitchenservice.exception.KitchenTicketNotFoundException;
import com.restaurant.kitchenservice.kafka.producer.KitchenProducer;
import com.restaurant.kitchenservice.repository.KitchenTicketRepository;
import com.restaurant.kitchenservice.service.KitchenService;
import com.restaurant.kitchenservice.service.KitchenTicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        KitchenTicket ticket = ticketRepository.findByOrderId(orderId)
                .orElseThrow(() -> new KitchenTicketNotFoundException(
                        "Kitchen ticket not found for orderId: " + orderId));
        return mapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTicketResponse> getPendingTickets() {
        return mapper.toResponseList(
                ticketRepository.findByStatusOrderByCreatedAtAsc(KitchenStatus.PENDING));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTicketResponse> getTicketsByStatus(KitchenStatus status) {
        return mapper.toResponseList(
                ticketRepository.findByStatusOrderByCreatedAtAsc(status));
    }

    @Override
    @Transactional(readOnly = true)
    public List<KitchenTicketResponse> getActiveTickets() {
        return mapper.toResponseList(
                ticketRepository.findAllActiveOrderByCreatedAtAsc());
    }

    // ─── Dashboard ─────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public KitchenStatsResponse getStats() {
        long pending     = ticketRepository.countByStatus(KitchenStatus.PENDING);
        long preparing   = ticketRepository.countByStatus(KitchenStatus.IN_PREPARATION);
        long ready       = ticketRepository.countByStatus(KitchenStatus.READY);
        long completed   = ticketRepository.countByStatus(KitchenStatus.COMPLETED);
        long cancelled   = ticketRepository.countByStatus(KitchenStatus.CANCELLED);

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
        return ticketRepository.findById(id)
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
