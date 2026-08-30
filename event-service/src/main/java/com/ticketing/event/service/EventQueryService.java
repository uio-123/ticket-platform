package com.ticketing.event.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ticketing.event.domain.po.Show;
import com.ticketing.event.domain.po.ShowSession;
import com.ticketing.event.domain.po.TicketTier;
import com.ticketing.event.mapper.ShowMapper;
import com.ticketing.event.mapper.ShowSessionMapper;
import com.ticketing.event.mapper.TicketTierMapper;
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
}
