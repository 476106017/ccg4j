package org.example.endpoint.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.game.BorderlandBattleLog;
import org.example.user.service.BorderlandBattleLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 弥留之国战斗记录API
 */
@RestController
@RequestMapping("/api/borderland/battle-logs")
@RequiredArgsConstructor
@Slf4j
public class BorderlandBattleLogController {
    
    private final BorderlandBattleLogService battleLogService;
    
    /**
     * 查询最近的战斗记录
     * @param limit 返回记录数量，默认10条
     */
    @GetMapping("/recent")
    public List<BorderlandBattleLog> getRecentLogs(
            @RequestParam(defaultValue = "10") int limit) {
        if (limit > 50) {
            limit = 50; // 最多返回50条
        }
        return battleLogService.getRecent(limit);
    }
    
    /**
     * 查询24小时内的战斗记录
     */
    @GetMapping("/last24hours")
    public List<BorderlandBattleLog> getLast24Hours() {
        return battleLogService.getLast24Hours();
    }
}
