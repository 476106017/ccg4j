package org.example.community.controller;

import lombok.RequiredArgsConstructor;
import org.example.auth.SessionConstants;
import org.example.community.entity.CommunityChannel;
import org.example.community.service.CommunityService;
import org.example.system.util.Result;
import org.example.user.service.UserAccountService;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityController {

    private final CommunityService communityService;
    private final UserAccountService userAccountService;

    private Long getUserId(HttpSession session) {
        Object userId = session.getAttribute(SessionConstants.SESSION_USER_ID);
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        return Long.valueOf(userId.toString());
    }

    private Long getUserIdOrNull(HttpSession session) {
        Object userId = session.getAttribute(SessionConstants.SESSION_USER_ID);
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    @GetMapping("/channels")
    public Result<List<CommunityChannel>> getChannels() {
        return Result.success(communityService.getChannels());
    }

    @PostMapping("/channels")
    public Result<Void> createChannel(@RequestBody Map<String, String> params, HttpSession session) {
        String name = params.get("name");
        String description = params.get("description");
        String type = params.get("type"); // PUBLIC, PRIVATE
        communityService.createChannel(name, description, type, getUserId(session));
        return Result.success();
    }

    @PutMapping("/channels/{id}")
    public Result<Void> updateChannel(@PathVariable Long id, @RequestBody Map<String, String> params,
            HttpSession session) {
        String name = params.get("name");
        String description = params.get("description");
        communityService.updateChannel(id, name, description, getUserId(session));
        return Result.success();
    }

    @DeleteMapping("/channels/{id}")
    public Result<Void> deleteChannel(@PathVariable Long id, HttpSession session) {
        communityService.deleteChannel(id, getUserId(session));
        return Result.success();
    }

    @PostMapping("/channels/{id}/pin")
    public Result<Void> pinChannel(@PathVariable Long id, HttpSession session) {
        communityService.pinChannel(id, getUserId(session));
        return Result.success();
    }

    @PostMapping("/channels/{id}/whitelist")
    public Result<Void> addToWhitelist(@PathVariable Long id, @RequestBody Map<String, Long> params,
            HttpSession session) {
        Long targetUserId = params.get("userId");
        communityService.updateChannelWhitelist(id, targetUserId, true, getUserId(session));
        return Result.success();
    }

    @DeleteMapping("/channels/{id}/whitelist/{userId}")
    public Result<Void> removeFromWhitelist(@PathVariable Long id, @PathVariable Long userId, HttpSession session) {
        communityService.updateChannelWhitelist(id, userId, false, getUserId(session));
        return Result.success();
    }

    @GetMapping("/channels/{channelId}/posts")
    public Result<List<Map<String, Object>>> getPosts(@PathVariable Long channelId) {
        return Result.success(communityService.getPosts(channelId));
    }

    @PostMapping("/posts")
    public Result<Void> createPost(@RequestBody Map<String, Object> params, HttpSession session) {
        Long channelId = Long.valueOf(params.get("channelId").toString());
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        communityService.createPost(channelId, title, content, getUserId(session));
        return Result.success();
    }

    @GetMapping("/posts/{postId}")
    public Result<Map<String, Object>> getPostDetail(@PathVariable Long postId, HttpSession session) {
        return Result.success(communityService.getPostDetail(postId, getUserIdOrNull(session)));
    }

    @PostMapping("/replies")
    public Result<Void> createReply(@RequestBody Map<String, Object> params, HttpSession session) {
        Long postId = Long.valueOf(params.get("postId").toString());
        Object parentIdObj = params.get("parentId");
        Long parentId = parentIdObj != null ? Long.valueOf(parentIdObj.toString()) : null;
        String content = (String) params.get("content");
        communityService.createReply(postId, parentId, content, getUserId(session));
        return Result.success();
    }

    @DeleteMapping("/replies/{replyId}")
    public Result<Void> deleteReply(@PathVariable Long replyId, HttpSession session) {
        communityService.deleteReply(replyId, getUserId(session));
        return Result.success();
    }

    @PostMapping("/vote")
    public Result<Void> vote(@RequestBody Map<String, Object> params, HttpSession session) {
        String targetType = (String) params.get("targetType");
        Long targetId = Long.valueOf(params.get("targetId").toString());
        int voteType = Integer.parseInt(params.get("voteType").toString());
        communityService.vote(targetType, targetId, voteType, getUserId(session));
        return Result.success();
    }

    @PostMapping("/channels/{channelId}/ban")
    public Result<Void> banUser(@PathVariable Long channelId, @RequestBody Map<String, Object> params,
            HttpSession session) {
        Long targetUserId = Long.valueOf(params.get("userId").toString());
        int durationType = Integer.parseInt(params.get("durationType").toString());
        communityService.banUser(channelId, targetUserId, durationType, getUserId(session));
        return Result.success();
    }
}
