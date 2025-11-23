package org.example.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.user.entity.ChatMessage;

@Mapper
public interface ChatMapper extends BaseMapper<ChatMessage> {
}
