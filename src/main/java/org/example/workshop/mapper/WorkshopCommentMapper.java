package org.example.workshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.workshop.entity.WorkshopComment;

@Mapper
public interface WorkshopCommentMapper extends BaseMapper<WorkshopComment> {
}
