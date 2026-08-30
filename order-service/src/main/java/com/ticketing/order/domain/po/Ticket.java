package com.ticketing.order.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ticket")
public class Ticket {
    public static final int ISSUED = 1;
    public static final int VERIFIED = 2;
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long audienceId;
    private String token;
    private Integer status;
    private LocalDateTime verifiedTime;
}
