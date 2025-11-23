package org.example.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.user.dto.FriendDto;
import org.example.user.entity.FriendRelationship;

import java.util.List;

public interface FriendService extends IService<FriendRelationship> {
    void sendFriendRequest(Long userId, String friendUsername);

    void acceptFriendRequest(Long userId, Long friendId);

    void rejectFriendRequest(Long userId, Long friendId);

    void removeFriend(Long userId, Long friendId);

    List<FriendDto> getFriendList(Long userId);

    // Block System
    void blockUser(Long userId, Long blockId);

    void unblockUser(Long userId, Long blockId);

    boolean isBlocked(Long userId, Long targetId);
}
