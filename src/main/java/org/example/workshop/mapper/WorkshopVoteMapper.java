package org.example.workshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.workshop.entity.WorkshopVote;

@Mapper
public interface WorkshopVoteMapper extends BaseMapper<WorkshopVote> {
}
