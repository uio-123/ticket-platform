package com.ticketing.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketing.order.domain.po.Ticket;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

public interface TicketMapper extends BaseMapper<Ticket> {
    @Update("UPDATE ticket SET status = 2, verified_time = #{verifiedTime} WHERE token = #{token} AND status = 1")
    int verifyOnce(@Param("token") String token, @Param("verifiedTime") LocalDateTime verifiedTime);
}
