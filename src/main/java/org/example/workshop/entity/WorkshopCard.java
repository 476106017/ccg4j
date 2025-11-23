package org.example.workshop.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("workshop_card")
public class WorkshopCard {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long authorId;
    private String name;
    private String description;
    private Integer cost;
    private Integer attack;
    private Integer health;
    private String cardType;
    private String job;
    private String race;
    private Integer countdown;
    private String imageUrl;
    private String status;
    private Integer likes;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
