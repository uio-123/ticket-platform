package com.ticketing.event.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("show_session")
public class ShowSession {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long showId;
    private String venue;
    private LocalDateTime saleStartTime;
    private LocalDateTime saleEndTime;
    private LocalDateTime showTime;
    private Integer status;
}
