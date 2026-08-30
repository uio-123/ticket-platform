package com.ticketing.order.controller;

import com.ticketing.order.domain.dto.TicketOrderCreateDTO;
import com.ticketing.order.domain.po.Ticket;
import com.ticketing.order.service.TicketOrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "票务订单、出票与验票接口")
@RestController
@RequestMapping("/ticket-orders")
@RequiredArgsConstructor
public class TicketOrderController {
    private final TicketOrderService ticketOrderService;
    @ApiOperation("创建待支付票务订单")
    @PostMapping public Long create(@RequestBody TicketOrderCreateDTO request) { return ticketOrderService.create(request); }
    @ApiOperation("模拟支付成功并出票")
    @PutMapping("/{orderId}/pay") public void pay(@PathVariable Long orderId) { ticketOrderService.pay(orderId); }
    @ApiOperation("取消订单并回补票源")
    @PutMapping("/{orderId}/cancel") public void cancel(@PathVariable Long orderId) { ticketOrderService.cancel(orderId); }
    @ApiOperation("查询订单电子票")
    @GetMapping("/{orderId}/tickets") public List<Ticket> tickets(@PathVariable Long orderId) { return ticketOrderService.listTickets(orderId); }
    @ApiOperation("一次性核验电子票")
    @PutMapping("/tickets/{token}/verify") public void verify(@PathVariable String token) { ticketOrderService.verify(token); }
}
