package com.ticketing.order.rush;

import com.ticketing.order.service.TicketOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TicketRushListener {
    private final TicketOrderService ticketOrderService;
    private final TicketRushService ticketRushService;
    private final StringRedisTemplate redisTemplate;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(name = "ticket.rush.request.queue", durable = "true"),
            exchange = @Exchange(name = "ticket.rush.direct"), key = "ticket.rush.request"))
    public void consume(TicketRushMessage message) {
        String consumedKey = "ticket:rush:consumed:" + message.getRequestId();
        if (!Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(consumedKey, "1"))) return;
        try {
            Long orderId = ticketOrderService.createForUser(message.getUserId(), message.getOrder());
            ticketRushService.markSuccess(message.getRequestId(), orderId);
        } catch (RuntimeException exception) {
            ticketRushService.markFailureAndRollback(message);
            throw exception;
        }
    }
}
