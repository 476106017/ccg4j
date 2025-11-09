package org.example.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.game.BorderlandBattleLog;

import java.util.List;

/**
 * 弥留之国战斗记录Mapper
 */
@Mapper
public interface BorderlandBattleLogMapper extends BaseMapper<BorderlandBattleLog> {
    
    /**
     * 查询最近的N条记录
     */
    @Select("SELECT * FROM borderland_battle_log ORDER BY timestamp DESC LIMIT #{limit}")
    List<BorderlandBattleLog> selectRecent(int limit);
    
    /**
     * 查询24小时内的记录
     */
    @Select("SELECT * FROM borderland_battle_log WHERE timestamp > NOW() - INTERVAL '24 hours' ORDER BY timestamp DESC")
    List<BorderlandBattleLog> selectLast24Hours();
}
