package com.ticketing.event.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ticketing.event.domain.po.Show;
import com.ticketing.event.domain.po.ShowSession;
import com.ticketing.event.domain.po.TicketTier;
import com.ticketing.event.mapper.ShowMapper;
import com.ticketing.event.mapper.ShowSessionMapper;
import com.ticketing.event.mapper.TicketTierMapper;
import com.ticketing.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventQueryService {
    private final ShowMapper showMapper;
    private final ShowSessionMapper showSessionMapper;
    private final TicketTierMapper ticketTierMapper;

    public List<Show> listPublishedShows() {
        return showMapper.selectList(new LambdaQueryWrapper<Show>()
                .eq(Show::getStatus, 1).orderByDesc(Show::getId));
    }

    public List<ShowSession> listSessions(Long showId) {
        return showSessionMapper.selectList(new LambdaQueryWrapper<ShowSession>()
                .eq(ShowSession::getShowId, showId).eq(ShowSession::getStatus, 1)
                .orderByAsc(ShowSession::getShowTime));
    }

    public List<TicketTier> listAvailableTiers(Long sessionId) {
        return ticketTierMapper.selectList(new LambdaQueryWrapper<TicketTier>()
                .eq(TicketTier::getSessionId, sessionId).eq(TicketTier::getStatus, 1)
                .orderByAsc(TicketTier::getPrice));
    }

    public TicketTier getTier(Long tierId) {
        TicketTier tier = ticketTierMapper.selectById(tierId);
        if (tier == null || tier.getStatus() != 1) {
            throw new BadRequestException("票档不存在或已停售");
        }
        return tier;
    }

    public void deductStock(Long tierId, Integer quantity) {
        if (quantity == null || quantity <= 0 || ticketTierMapper.deductAvailableStock(tierId, quantity) != 1) {
            throw new BadRequestException("票档余量不足");
        }
    }

    public void restoreStock(Long tierId, Integer quantity) {
        if (quantity == null || quantity <= 0 || ticketTierMapper.restoreAvailableStock(tierId, quantity) != 1) {
            throw new BadRequestException("票档库存回补失败");
        }
    }
}
