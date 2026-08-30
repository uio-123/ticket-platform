package com.ticketing.event.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketing.event.domain.po.TicketTier;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface TicketTierMapper extends BaseMapper<TicketTier> {
    @Update("UPDATE ticket_tier SET available_stock = available_stock - #{quantity} "
            + "WHERE id = #{tierId} AND status = 1 AND available_stock >= #{quantity}")
    int deductAvailableStock(@Param("tierId") Long tierId, @Param("quantity") Integer quantity);

    @Update("UPDATE ticket_tier SET available_stock = available_stock + #{quantity} "
            + "WHERE id = #{tierId} AND available_stock + #{quantity} <= total_stock")
    int restoreAvailableStock(@Param("tierId") Long tierId, @Param("quantity") Integer quantity);
}
