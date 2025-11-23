package org.example.workshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("workshop_comment")
public class WorkshopComment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long cardId;
    private Long authorId;
    private String content;
    private OffsetDateTime createdAt;
}
