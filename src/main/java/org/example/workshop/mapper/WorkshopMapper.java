package org.example.workshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.workshop.entity.WorkshopCard;
import org.example.workshop.entity.WorkshopComment;
import org.example.workshop.entity.WorkshopVote;

@Mapper
public interface WorkshopMapper extends BaseMapper<WorkshopCard> {
    // Custom methods can be added here if needed, but BaseMapper covers most
}
