package org.example.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("community_reply")
public class CommunityReply {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long parentId; // null for top-level reply
    private Long authorId;
    private String content;
    private OffsetDateTime createdAt;

    // Voting
    private Integer upvotes;
    private Integer downvotes;
}
