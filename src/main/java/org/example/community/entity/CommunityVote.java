package org.example.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("community_vote")
public class CommunityVote {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String targetType; // POST, REPLY
    private Long targetId;
    private Integer voteType; // 1: Up, -1: Down
}
