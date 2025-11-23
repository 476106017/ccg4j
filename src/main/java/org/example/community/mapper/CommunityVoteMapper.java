package org.example.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.community.entity.CommunityVote;

@Mapper
public interface CommunityVoteMapper extends BaseMapper<CommunityVote> {
}
