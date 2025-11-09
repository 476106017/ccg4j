package org.example.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.game.BorderlandBattleLog;
import org.example.user.mapper.BorderlandBattleLogMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 弥留之国战斗记录服务
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BorderlandBattleLogService {

    private final BorderlandBattleLogMapper battleLogMapper;

    /**
     * 保存战斗记录
     */
    public void save(BorderlandBattleLog msg) {
        battleLogMapper.insert(msg);
        log.info("保存战斗记录: {} vs {}, 类型: {}",
                msg.getPlayer1Name(), msg.getPlayer2Name(), msg.getEventType());
    }

    /**
     * 查询最近的N条记录
     */
    public List<BorderlandBattleLog> getRecent(int limit) {
        return battleLogMapper.selectRecent(limit);
    }

    /**
     * 查询24小时内的记录
     */
    public List<BorderlandBattleLog> getLast24Hours() {
        return battleLogMapper.selectLast24Hours();
    }
}
