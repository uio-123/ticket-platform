package com.ticketing.item.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ticketing.api.dto.ItemDTO;
import com.ticketing.api.dto.OrderDetailDTO;
import com.ticketing.item.domain.po.Item;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 商品表 服务类
 * </p>
 *
 * @author Ticketing Team
 * @since 2023-05-05
 */
public interface IItemService extends IService<Item> {

    void deductStock(List<OrderDetailDTO> items);

    List<ItemDTO> queryItemByIds(Collection<Long> ids);
}
