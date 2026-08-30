package com.ticketing.order.controller;

import com.ticketing.api.client.TicketTierClient;
import com.ticketing.api.dto.TicketTierDTO;
import com.ticketing.common.exception.BadRequestException;
import com.ticketing.common.utils.UserContext;
import com.ticketing.order.domain.dto.TicketOrderCreateDTO;
import com.ticketing.order.rush.TicketRushService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Api(tags = "高并发抢票接口")
@RestController
@RequestMapping("/ticket-rush")
@RequiredArgsConstructor
public class TicketRushController {
    private final TicketRushService ticketRushService;
    private final TicketTierClient ticketTierClient;
    @ApiOperation("开售前预热 Redis 余票")
    @PutMapping("/sessions/{sessionId}/tiers/{tierId}/stock")
    public void preload(@PathVariable Long sessionId, @PathVariable Long tierId, @RequestParam Integer stock) { ticketRushService.preload(sessionId, tierId, stock); }
    @ApiOperation("Lua 原子预扣库存并异步建单")
    @PostMapping
    public String rush(@RequestBody TicketOrderCreateDTO order) {
        Long userId = UserContext.getUser();
        if (userId == null) throw new BadRequestException("请先登录");
        TicketTierDTO tier = ticketTierClient.queryTier(order.getTierId());
        if (tier == null || !order.getSessionId().equals(tier.getSessionId())) throw new BadRequestException("票档不存在");
        return ticketRushService.reserveAndQueue(userId, order, tier);
    }
    @ApiOperation("查询抢票排队结果")
    @GetMapping("/{requestId}")
    public String result(@PathVariable String requestId) { return ticketRushService.result(requestId); }
}
