package org.example.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.community.entity.CommunityPost;

@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
}
