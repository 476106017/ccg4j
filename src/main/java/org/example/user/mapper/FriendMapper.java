package org.example.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.user.entity.FriendRelationship;

@Mapper
public interface FriendMapper extends BaseMapper<FriendRelationship> {
}
