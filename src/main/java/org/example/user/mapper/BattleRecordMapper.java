package org.example.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.user.entity.BattleRecord;

/**
 * 对战记录Mapper
 */
@Mapper
public interface BattleRecordMapper extends BaseMapper<BattleRecord> {
}
