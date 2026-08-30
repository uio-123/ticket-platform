package com.ticketing.api.client;

import com.ticketing.api.dto.TicketTierDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("event-service")
public interface TicketTierClient {
    @GetMapping("/ticket-tiers/{tierId}")
    TicketTierDTO queryTier(@PathVariable("tierId") Long tierId);

    @PutMapping("/ticket-tiers/{tierId}/stock/deduct")
    void deductStock(@PathVariable("tierId") Long tierId, @RequestParam("quantity") Integer quantity);

    @PutMapping("/ticket-tiers/{tierId}/stock/restore")
    void restoreStock(@PathVariable("tierId") Long tierId, @RequestParam("quantity") Integer quantity);
}
