package org.example.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.example.user.dto.FriendDto;
import org.example.user.entity.FriendRelationship;
import org.example.user.entity.UserAccount;
import org.example.user.mapper.FriendMapper;
import org.example.user.service.FriendService;
import org.example.user.service.UserAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.example.system.Database.userNames;

@Service
@RequiredArgsConstructor
public class FriendServiceImpl extends ServiceImpl<FriendMapper, FriendRelationship> implements FriendService {

    private final UserAccountService userAccountService;

    @Override
    @Transactional
    public void sendFriendRequest(Long userId, String friendUsername) {
        UserAccount friend = userAccountService.findByUsername(friendUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userId.equals(friend.getId())) {
            throw new RuntimeException("Cannot add yourself as friend");
        }

        // Check if relationship already exists
        boolean exists = lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(FriendRelationship::getUserId, userId).eq(FriendRelationship::getFriendId, friend.getId())
                        .or()
                        .eq(FriendRelationship::getUserId, friend.getId()).eq(FriendRelationship::getFriendId, userId))
                .exists();

        if (exists) {
            throw new RuntimeException("Friend request already sent or already friends");
        }

        FriendRelationship relationship = new FriendRelationship();
        relationship.setUserId(userId);
        relationship.setFriendId(friend.getId());
        relationship.setStatus("PENDING");
        save(relationship);
    }

    @Override
    @Transactional
    public void acceptFriendRequest(Long userId, Long friendId) {
        // Find the request where friendId is the sender (userId in DB) and userId is
        // the receiver (friendId in DB)
        // Wait, in sendFriendRequest: userId(Sender) -> friendId(Receiver)
        // So here, userId is the Receiver, friendId is the Sender.
        // We need to find record where userId=friendId and friendId=userId

        FriendRelationship relationship = lambdaQuery()
                .eq(FriendRelationship::getUserId, friendId)
                .eq(FriendRelationship::getFriendId, userId)
                .eq(FriendRelationship::getStatus, "PENDING")
                .oneOpt()
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        relationship.setStatus("ACCEPTED");
        updateById(relationship);

        // Create reverse relationship for easier querying?
        // Or just keep one record. Let's keep one record and handle querying.
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Long userId, Long friendId) {
        // userId is the Receiver, friendId is the Sender
        lambdaUpdate()
                .eq(FriendRelationship::getUserId, friendId)
                .eq(FriendRelationship::getFriendId, userId)
                .eq(FriendRelationship::getStatus, "PENDING")
                .remove();
    }

    @Override
    @Transactional
    public void removeFriend(Long userId, Long friendId) {
        lambdaUpdate()
                .and(wrapper -> wrapper
                        .eq(FriendRelationship::getUserId, userId).eq(FriendRelationship::getFriendId, friendId)
                        .or()
                        .eq(FriendRelationship::getUserId, friendId).eq(FriendRelationship::getFriendId, userId))
                .remove();
    }

    @Override
    public List<FriendDto> getFriendList(Long userId) {
        // Find all relationships where user is involved
        List<FriendRelationship> relationships = lambdaQuery()
                .and(wrapper -> wrapper
                        .eq(FriendRelationship::getUserId, userId)
                        .or()
                        .eq(FriendRelationship::getFriendId, userId))
                .list();

        List<Long> friendIds = new ArrayList<>();
        for (FriendRelationship rel : relationships) {
            if (rel.getUserId().equals(userId)) {
                friendIds.add(rel.getFriendId());
            } else {
                friendIds.add(rel.getUserId());
            }
        }

        if (friendIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<UserAccount> friends = userAccountService.listByIds(friendIds);
        Map<Long, UserAccount> friendMap = friends.stream().collect(Collectors.toMap(UserAccount::getId, u -> u));

        return relationships.stream().map(rel -> {
            Long fid = rel.getUserId().equals(userId) ? rel.getFriendId() : rel.getUserId();
            UserAccount f = friendMap.get(fid);
            if (f == null)
                return null;

            FriendDto dto = new FriendDto();
            dto.setId(f.getId());
            dto.setUsername(f.getUsername());
            dto.setStatus(rel.getStatus());

            boolean isOnline = userNames.containsValue(f.getUsername());
            dto.setOnline(isOnline);

            return dto;
        }).filter(java.util.Objects::nonNull).collect(Collectors.toList());
    }

    // Block System
    private final org.example.user.mapper.UserBlockMapper userBlockMapper;

    @Override
    @Transactional
    public void blockUser(Long userId, Long blockId) {
        if (userId.equals(blockId))
            return;

        // Remove friend if exists
        removeFriend(userId, blockId);

        // Check if already blocked
        Long count = userBlockMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.example.user.entity.UserBlock>()
                        .eq(org.example.user.entity.UserBlock::getUserId, userId)
                        .eq(org.example.user.entity.UserBlock::getBlockedUserId, blockId));

        if (count == null || count == 0) {
            org.example.user.entity.UserBlock block = new org.example.user.entity.UserBlock();
            block.setUserId(userId);
            block.setBlockedUserId(blockId);
            userBlockMapper.insert(block);
        }
    }

    @Override
    @Transactional
    public void unblockUser(Long userId, Long blockId) {
        userBlockMapper.delete(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.example.user.entity.UserBlock>()
                        .eq(org.example.user.entity.UserBlock::getUserId, userId)
                        .eq(org.example.user.entity.UserBlock::getBlockedUserId, blockId));
    }

    @Override
    public boolean isBlocked(Long userId, Long targetId) {
        Long count = userBlockMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<org.example.user.entity.UserBlock>()
                        .eq(org.example.user.entity.UserBlock::getUserId, userId)
                        .eq(org.example.user.entity.UserBlock::getBlockedUserId, targetId));
        return count != null && count > 0;
    }
}
