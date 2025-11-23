package org.example.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("friend_relationship")
public class FriendRelationship {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long friendId;
    private String status; // PENDING, ACCEPTED, BLOCKED
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
