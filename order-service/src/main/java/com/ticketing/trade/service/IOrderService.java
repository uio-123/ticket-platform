package com.ticketing.trade.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketing.trade.domain.dto.OrderFormDTO;
import com.ticketing.trade.domain.po.Order;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Ticketing Team
 * @since 2023-05-05
 */
public interface IOrderService extends IService<Order> {

    Long createOrder(OrderFormDTO orderFormDTO);

    void markOrderPaySuccess(Long orderId);

    void cancelOrder(Long orderId);
}
