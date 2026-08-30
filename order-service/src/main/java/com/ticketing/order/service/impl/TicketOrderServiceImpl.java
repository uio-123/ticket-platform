package com.ticketing.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ticketing.api.client.TicketTierClient;
import com.ticketing.api.dto.TicketTierDTO;
import com.ticketing.common.exception.BadRequestException;
import com.ticketing.common.utils.UserContext;
import com.ticketing.order.domain.dto.TicketOrderCreateDTO;
import com.ticketing.order.domain.po.Ticket;
import com.ticketing.order.domain.po.TicketOrder;
import com.ticketing.order.domain.po.TicketOrderItem;
import com.ticketing.order.mapper.TicketMapper;
import com.ticketing.order.mapper.TicketOrderItemMapper;
import com.ticketing.order.mapper.TicketOrderMapper;
import com.ticketing.order.service.TicketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketOrderServiceImpl implements TicketOrderService {
    private final TicketTierClient ticketTierClient;
    private final TicketOrderMapper ticketOrderMapper;
    private final TicketOrderItemMapper ticketOrderItemMapper;
    private final TicketMapper ticketMapper;

    @Override
    @Transactional
    public Long create(TicketOrderCreateDTO request) {
        Long userId = UserContext.getUser();
        if (userId == null) throw new BadRequestException("请先登录");
        return createForUser(userId, request);
    }

    @Override
    @Transactional
    public Long createForUser(Long userId, TicketOrderCreateDTO request) {
        if (request.getQuantity() == null || request.getQuantity() <= 0) throw new BadRequestException("购票数量必须大于 0");
        TicketTierDTO tier = ticketTierClient.queryTier(request.getTierId());
        if (tier == null || !request.getSessionId().equals(tier.getSessionId())) throw new BadRequestException("票档不存在");
        if (ticketOrderMapper.countPurchasedQuantity(userId, request.getSessionId()) + request.getQuantity() > tier.getPurchaseLimit()) {
            throw new BadRequestException("超过场次限购数量");
        }
        ticketTierClient.deductStock(tier.getId(), request.getQuantity());
        try {
            TicketOrder order = new TicketOrder();
            order.setUserId(userId); order.setSessionId(request.getSessionId()); order.setAudienceId(request.getAudienceId());
            order.setTotalAmount(tier.getPrice().multiply(java.math.BigDecimal.valueOf(request.getQuantity())));
            order.setStatus(TicketOrder.PENDING_PAYMENT); order.setCreateTime(LocalDateTime.now());
            ticketOrderMapper.insert(order);
            TicketOrderItem item = new TicketOrderItem();
            item.setOrderId(order.getId()); item.setTierId(tier.getId()); item.setTierName(tier.getName());
            item.setUnitPrice(tier.getPrice()); item.setQuantity(request.getQuantity());
            ticketOrderItemMapper.insert(item);
            return order.getId();
        } catch (RuntimeException exception) {
            ticketTierClient.restoreStock(tier.getId(), request.getQuantity());
            throw exception;
        }
    }

    @Override
    @Transactional
    public void pay(Long orderId) {
        if (ticketOrderMapper.markPaidIfPending(orderId, LocalDateTime.now()) == 1) issueTickets(orderId);
    }

    @Override
    @Transactional
    public void cancel(Long orderId) {
        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null || ticketOrderMapper.cancelIfPending(orderId, LocalDateTime.now()) != 1) return;
        TicketOrderItem item = ticketOrderItemMapper.selectOne(new LambdaQueryWrapper<TicketOrderItem>().eq(TicketOrderItem::getOrderId, orderId));
        ticketTierClient.restoreStock(item.getTierId(), item.getQuantity());
    }

    @Override
    @Scheduled(fixedDelay = 60000)
    public void closeExpiredOrders() {
        ticketOrderMapper.findExpired(LocalDateTime.now().minusMinutes(15)).forEach(order -> cancel(order.getId()));
    }

    @Override
    @Transactional
    public List<Ticket> issueTickets(Long orderId) {
        List<Ticket> existing = ticketMapper.selectList(new LambdaQueryWrapper<Ticket>().eq(Ticket::getOrderId, orderId));
        if (!existing.isEmpty()) return existing;
        TicketOrder order = ticketOrderMapper.selectById(orderId);
        if (order == null || order.getStatus() != TicketOrder.PAID) throw new BadRequestException("仅已支付订单可以出票");
        TicketOrderItem item = ticketOrderItemMapper.selectOne(new LambdaQueryWrapper<TicketOrderItem>().eq(TicketOrderItem::getOrderId, orderId));
        List<Ticket> tickets = new ArrayList<>();
        for (int i = 0; i < item.getQuantity(); i++) {
            Ticket ticket = new Ticket();
            ticket.setOrderId(orderId); ticket.setAudienceId(order.getAudienceId());
            ticket.setToken(UUID.randomUUID().toString().replace("-", "")); ticket.setStatus(Ticket.ISSUED);
            ticketMapper.insert(ticket); tickets.add(ticket);
        }
        return tickets;
    }

    @Override
    public List<Ticket> listTickets(Long orderId) {
        return ticketMapper.selectList(new LambdaQueryWrapper<Ticket>().eq(Ticket::getOrderId, orderId));
    }

    @Override
    public void verify(String token) {
        if (ticketMapper.verifyOnce(token, LocalDateTime.now()) != 1) throw new BadRequestException("电子票不存在或已核验");
    }
}
