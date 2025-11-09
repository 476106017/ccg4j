package org.example.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.system.entity.ConfigDict;

/**
 * 配置字典Mapper
 */
@Mapper
public interface ConfigDictMapper extends BaseMapper<ConfigDict> {
    
    /**
     * 根据配置键查询配置值
     */
    @Select("SELECT config_value FROM config_dict WHERE config_key = #{configKey}")
    String getValueByKey(String configKey);
}
