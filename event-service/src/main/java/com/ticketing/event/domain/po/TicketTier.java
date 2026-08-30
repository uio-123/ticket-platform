package com.ticketing.event.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("ticket_tier")
public class TicketTier {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private String name;
    private BigDecimal price;
    private Integer totalStock;
    private Integer availableStock;
    private Integer purchaseLimit;
    private Integer status;
}
