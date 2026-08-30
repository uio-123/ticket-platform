package com.ticketing.order.rush;

import com.ticketing.order.domain.dto.TicketOrderCreateDTO;
import lombok.Data;

import java.io.Serializable;

@Data
public class TicketRushMessage implements Serializable {
    private String requestId;
    private Long userId;
    private TicketOrderCreateDTO order;
}
