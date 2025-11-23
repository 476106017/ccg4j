package org.example.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("community_post")
public class CommunityPost {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long channelId;
    private Long authorId;
    private String title;
    private String content;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastReplyAt;

    // Voting
    private Integer upvotes;
    private Integer downvotes;
}
