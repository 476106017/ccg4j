package org.example.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@TableName("user_account")
public class UserAccount {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String username;
    private String password;
    private Integer tickets;
    private Integer arcaneDust;
    private Integer matchRating;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
