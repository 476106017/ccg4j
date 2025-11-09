package org.example.game;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 弥留之国战斗记录
 */
@Data
@TableName("borderland_battle_log")
public class BorderlandBattleLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 事件类型：match-匹配, victory-胜利, defeat-失败
     */
    private String eventType;
    
    /**
     * 玩家1名称
     */
    private String player1Name;
    
    /**
     * 玩家2名称
     */
    private String player2Name;
    
    /**
     * 胜者名称（仅victory/defeat时有值）
     */
    private String winnerName;
    
    /**
     * 时间戳
     */
    private LocalDateTime timestamp;
    
    /**
     * 惩罚时间(秒)
     */
    private Integer punishmentSeconds;

    public BorderlandBattleLog() {}

    public BorderlandBattleLog(String eventType, String player1Name, String player2Name, 
                               String winnerName, Integer punishmentSeconds) {
        this.eventType = eventType;
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.winnerName = winnerName;
        this.timestamp = LocalDateTime.now();
        this.punishmentSeconds = punishmentSeconds;
    }
}
