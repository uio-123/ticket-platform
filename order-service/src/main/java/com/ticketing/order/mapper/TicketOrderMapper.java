package com.ticketing.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ticketing.order.domain.po.TicketOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketOrderMapper extends BaseMapper<TicketOrder> {
    @Select("SELECT COALESCE(SUM(i.quantity), 0) FROM ticket_order o JOIN ticket_order_item i ON i.order_id = o.id "
            + "WHERE o.user_id = #{userId} AND o.session_id = #{sessionId} AND o.status IN (1, 2)")
    int countPurchasedQuantity(@Param("userId") Long userId, @Param("sessionId") Long sessionId);

    @Update("UPDATE ticket_order SET status = 3, close_time = #{closeTime} WHERE id = #{orderId} AND status = 1")
    int cancelIfPending(@Param("orderId") Long orderId, @Param("closeTime") LocalDateTime closeTime);

    @Update("UPDATE ticket_order SET status = 2, pay_time = #{payTime} WHERE id = #{orderId} AND status = 1")
    int markPaidIfPending(@Param("orderId") Long orderId, @Param("payTime") LocalDateTime payTime);

    @Select("SELECT * FROM ticket_order WHERE status = 1 AND create_time < #{deadline}")
    List<TicketOrder> findExpired(@Param("deadline") LocalDateTime deadline);
}
