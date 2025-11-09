package org.example.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 对战记录表
 */
@Data
@TableName("battle_record")
public class BattleRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 获胜者ID（AI为null）
     */
    private Long winnerId;
    
    /**
     * 失败者ID（AI为null）
     */
    private Long loserId;
    
    /**
     * 对战模式：normal-常规匹配, borderland-弥留之国
     */
    private String mode;
    
    /**
     * 获胜者卡组（逗号分隔的卡牌代码）
     */
    private String winnerDeck;
    
    /**
     * 失败者卡组（逗号分隔的卡牌代码）
     */
    private String loserDeck;
    
    /**
     * 获胜者职业
     */
    private String winnerLeader;
    
    /**
     * 失败者职业
     */
    private String loserLeader;
    
    /**
     * 对战持续时间（秒）
     */
    private Integer duration;
    
    /**
     * 回合数
     */
    private Integer totalTurns;
    
    /**
     * 结束原因：hp_zero-生命归零, concede-投降, timeout-超时, special-特殊胜利（如麻将桌）
     */
    private String endReason;
    
    /**
     * 对战详情（JSON格式）：记录关键操作、最终场面等
     */
    private String battleDetails;
    
    /**
     * 创建时间
     */
    private OffsetDateTime createdAt;
}
