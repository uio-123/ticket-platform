package com.ticketing.event.controller;

import com.ticketing.event.domain.po.Show;
import com.ticketing.event.domain.po.ShowSession;
import com.ticketing.event.domain.po.TicketTier;
import com.ticketing.event.service.EventQueryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "演出、场次与票档接口")
@RestController
@RequestMapping("/shows")
@RequiredArgsConstructor
public class EventController {
    private final EventQueryService eventQueryService;
    @ApiOperation("查询在售演出")
    @GetMapping
    public List<Show> listShows() { return eventQueryService.listPublishedShows(); }
    @ApiOperation("查询演出场次")
    @GetMapping("/{showId}/sessions")
    public List<ShowSession> listSessions(@PathVariable Long showId) { return eventQueryService.listSessions(showId); }
    @ApiOperation("查询场次可售票档")
    @GetMapping("/sessions/{sessionId}/ticket-tiers")
    public List<TicketTier> listTiers(@PathVariable Long sessionId) { return eventQueryService.listAvailableTiers(sessionId); }

    @ApiOperation("查询票档余量")
    @GetMapping("/ticket-tiers/{tierId}")
    public TicketTier getTier(@PathVariable Long tierId) { return eventQueryService.getTier(tierId); }

    @ApiOperation("原子扣减票档余量")
    @PutMapping("/ticket-tiers/{tierId}/stock/deduct")
    public void deductStock(@PathVariable Long tierId, @RequestParam Integer quantity) { eventQueryService.deductStock(tierId, quantity); }

    @ApiOperation("回补票档余量")
    @PutMapping("/ticket-tiers/{tierId}/stock/restore")
    public void restoreStock(@PathVariable Long tierId, @RequestParam Integer quantity) { eventQueryService.restoreStock(tierId, quantity); }
}
