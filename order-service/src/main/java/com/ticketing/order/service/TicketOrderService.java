package com.ticketing.order.service;

import com.ticketing.order.domain.dto.TicketOrderCreateDTO;
import com.ticketing.order.domain.po.Ticket;
import com.ticketing.order.domain.po.TicketOrder;

import java.util.List;

public interface TicketOrderService {
    Long create(TicketOrderCreateDTO request);
    Long createForUser(Long userId, TicketOrderCreateDTO request);
    void pay(Long orderId);
    void cancel(Long orderId);
    void closeExpiredOrders();
    List<Ticket> issueTickets(Long orderId);
    List<Ticket> listTickets(Long orderId);
    void verify(String token);
}
