package org.example.community.service;

import org.example.community.entity.*;
import java.util.List;
import java.util.Map;

public interface CommunityService {
    // Channel
    List<CommunityChannel> getChannels();

    void createChannel(String name, String description, String type, Long userId);

    void updateChannel(Long channelId, String name, String description, Long userId);

    void deleteChannel(Long channelId, Long userId);

    void pinChannel(Long channelId, Long userId);

    void updateChannelWhitelist(Long channelId, Long targetUserId, boolean add, Long operatorId);

    // Post
    List<Map<String, Object>> getPosts(Long channelId);

    Map<String, Object> getPostDetail(Long postId, Long userId);

    void createPost(Long channelId, String title, String content, Long userId);

    // Reply
    void createReply(Long postId, Long parentId, String content, Long userId);

    void deleteReply(Long replyId, Long userId);

    // Vote
    void vote(String targetType, Long targetId, int voteType, Long userId);

    // Ban
    void banUser(Long channelId, Long targetUserId, int durationType, Long operatorId);
}
