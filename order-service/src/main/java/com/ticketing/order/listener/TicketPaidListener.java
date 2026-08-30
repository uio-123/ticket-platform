package com.ticketing.order.listener;

import com.ticketing.order.service.TicketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketPaidListener {
    private final TicketOrderService ticketOrderService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "ticket.pay.success.queue", durable = "true"),
            exchange = @Exchange(name = "ticket.pay.direct"),
            key = "ticket.pay.success"))
    public void onPaymentSuccess(Long orderId) {
        ticketOrderService.pay(orderId);
    }
}
