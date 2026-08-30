package com.ticketing.order.rush;

import com.ticketing.api.dto.TicketTierDTO;
import com.ticketing.common.exception.BadRequestException;
import com.ticketing.order.domain.dto.TicketOrderCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketRushService {
    // 单次 Lua 调用完成余票和限购校验，避免热点请求并发穿透到数据库。
    private static final String EXCHANGE = "ticket.rush.direct";
    private static final String ROUTING_KEY = "ticket.rush.request";
    private static final DefaultRedisScript<Long> RESERVE_SCRIPT = new DefaultRedisScript<>(
            "local stock=tonumber(redis.call('GET', KEYS[1]) or '-1'); "
                    + "local bought=tonumber(redis.call('GET', KEYS[2]) or '0'); "
                    + "local count=tonumber(ARGV[1]); local limit=tonumber(ARGV[2]); "
                    + "if stock < 0 then return 3 end; if stock < count then return 1 end; "
                    + "if bought + count > limit then return 2 end; "
                    + "redis.call('DECRBY', KEYS[1], count); redis.call('INCRBY', KEYS[2], count); return 0;", Long.class);

    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    public void preload(Long sessionId, Long tierId, Integer stock) {
        if (stock == null || stock < 0) throw new BadRequestException("预热库存无效");
        redisTemplate.opsForValue().set(stockKey(sessionId, tierId), String.valueOf(stock));
    }

    public String reserveAndQueue(Long userId, TicketOrderCreateDTO order, TicketTierDTO tier) {
        Long result = redisTemplate.execute(RESERVE_SCRIPT, Arrays.asList(stockKey(order.getSessionId(), order.getTierId()),
                userKey(order.getSessionId(), userId)), String.valueOf(order.getQuantity()), String.valueOf(tier.getPurchaseLimit()));
        if (result == null || result == 3) throw new BadRequestException("场次尚未预热或未开售");
        if (result == 1) throw new BadRequestException("票已售罄");
        if (result == 2) throw new BadRequestException("超过场次限购数量");
        String requestId = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(resultKey(requestId), "QUEUED", Duration.ofMinutes(20));
        TicketRushMessage message = new TicketRushMessage();
        message.setRequestId(requestId); message.setUserId(userId); message.setOrder(order);
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);
        return requestId;
    }

    public void markSuccess(String requestId, Long orderId) { redisTemplate.opsForValue().set(resultKey(requestId), "SUCCESS:" + orderId, Duration.ofMinutes(20)); }
    public void markFailureAndRollback(TicketRushMessage message) {
        // 只有异步建单失败才回补；成功订单的库存由数据库订单链路确认。
        TicketOrderCreateDTO order = message.getOrder();
        redisTemplate.opsForValue().increment(stockKey(order.getSessionId(), order.getTierId()), order.getQuantity());
        redisTemplate.opsForValue().increment(userKey(order.getSessionId(), message.getUserId()), -order.getQuantity());
        redisTemplate.opsForValue().set(resultKey(message.getRequestId()), "FAILED", Duration.ofMinutes(20));
    }
    public String result(String requestId) { return redisTemplate.opsForValue().get(resultKey(requestId)); }
    private String stockKey(Long sessionId, Long tierId) { return "ticket:stock:" + sessionId + ":" + tierId; }
    private String userKey(Long sessionId, Long userId) { return "ticket:limit:" + sessionId + ":" + userId; }
    private String resultKey(String requestId) { return "ticket:rush:result:" + requestId; }
}
