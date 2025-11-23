package org.example.user.dto;

import lombok.Data;

@Data
public class FriendDto {
    private Long id;
    private String username;
    private String status; // PENDING, ACCEPTED
    private boolean online;
}
