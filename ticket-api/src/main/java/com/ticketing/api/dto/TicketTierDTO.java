package com.ticketing.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketTierDTO {
    private Long id;
    private Long sessionId;
    private String name;
    private BigDecimal price;
    private Integer availableStock;
    private Integer purchaseLimit;
    private Integer status;
}
