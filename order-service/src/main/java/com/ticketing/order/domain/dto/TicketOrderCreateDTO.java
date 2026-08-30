package com.ticketing.order.domain.dto;

import lombok.Data;

@Data
public class TicketOrderCreateDTO {
    private Long sessionId;
    private Long tierId;
    private Long audienceId;
    private Integer quantity;
}
