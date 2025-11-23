package org.example.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.community.entity.*;
import org.example.community.mapper.*;
import org.example.community.service.CommunityService;
import org.example.user.entity.UserAccount;
import org.example.user.service.UserAccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommunityServiceImpl implements CommunityService {

    private final CommunityChannelMapper channelMapper;
    private final CommunityChannelWhitelistMapper whitelistMapper;
    private final CommunityPostMapper postMapper;
    private final CommunityReplyMapper replyMapper;
    private final CommunityVoteMapper voteMapper;
    private final CommunityBanMapper banMapper;
    private final UserAccountService userAccountService;

    private static final String ADMIN_USERNAME = "admin";

    private boolean isAdmin(Long userId) {
        UserAccount user = userAccountService.getById(userId);
        return user != null && ADMIN_USERNAME.equals(user.getUsername());
    }

    private void checkBan(Long channelId, Long userId) {
        CommunityBan ban = banMapper.selectOne(new LambdaQueryWrapper<CommunityBan>()
                .eq(CommunityBan::getChannelId, channelId)
                .eq(CommunityBan::getUserId, userId)
                .gt(CommunityBan::getEndTime, OffsetDateTime.now()));
        if (ban != null) {
            throw new RuntimeException("你已被该频道禁言，解封时间：" + ban.getEndTime());
        }
    }

    private void checkWhitelist(CommunityChannel channel, Long userId) {
        if (Boolean.TRUE.equals(channel.getIsRestricted())) {
            if (isAdmin(userId) || userId.equals(channel.getOwnerId())) {
                return;
            }
            Long count = whitelistMapper.selectCount(new LambdaQueryWrapper<CommunityChannelWhitelist>()
                    .eq(CommunityChannelWhitelist::getChannelId, channel.getId())
                    .eq(CommunityChannelWhitelist::getUserId, userId));
            if (count == 0) {
                throw new RuntimeException("该频道仅限白名单用户发言");
            }
        }
    }

    @Override
    public List<CommunityChannel> getChannels() {
        return channelMapper.selectList(new LambdaQueryWrapper<CommunityChannel>()
                .orderByDesc(CommunityChannel::getIsPinned)
                .orderByDesc(CommunityChannel::getLastActivityAt));
    }

    @Override
    @Transactional
    public void createChannel(String name, String description, String type, Long userId) {
        if ("PUBLIC".equals(type) && !isAdmin(userId)) {
            throw new RuntimeException("只有管理员可以创建公共频道");
        }
        if ("PRIVATE".equals(type)) {
            Long count = channelMapper.selectCount(new LambdaQueryWrapper<CommunityChannel>()
                    .eq(CommunityChannel::getOwnerId, userId)
                    .eq(CommunityChannel::getType, "PRIVATE"));
            if (count > 0) {
                throw new RuntimeException("每位用户只能创建一个私人频道");
            }
        }

        CommunityChannel channel = new CommunityChannel();
        channel.setName(name);
        channel.setDescription(description);
        channel.setType(type);
        channel.setOwnerId("PRIVATE".equals(type) ? userId : null);
        channel.setCreatedAt(OffsetDateTime.now());
        channel.setLastActivityAt(OffsetDateTime.now());
        channel.setIsPinned(false);
        channel.setLevel(1);
        channel.setHeat(0L);
        channel.setIsRestricted(false);

        channelMapper.insert(channel);
    }

    @Override
    @Transactional
    public void updateChannel(Long channelId, String name, String description, Long userId) {
        CommunityChannel channel = channelMapper.selectById(channelId);
        if (channel == null)
            throw new RuntimeException("频道不存在");

        if (!isAdmin(userId) && !userId.equals(channel.getOwnerId())) {
            throw new RuntimeException("无权修改频道信息");
        }

        channel.setName(name);
        channel.setDescription(description);
        channelMapper.updateById(channel);
    }

    @Override
    @Transactional
    public void deleteChannel(Long channelId, Long userId) {
        if (!isAdmin(userId))
            throw new RuntimeException("只有管理员可以删除频道");
        channelMapper.deleteById(channelId);
    }

    @Override
    @Transactional
    public void pinChannel(Long channelId, Long userId) {
        if (!isAdmin(userId))
            throw new RuntimeException("只有管理员可以置顶频道");
        CommunityChannel channel = channelMapper.selectById(channelId);
        if (channel != null) {
            channel.setIsPinned(!Boolean.TRUE.equals(channel.getIsPinned()));
            channelMapper.updateById(channel);
        }
    }

    @Override
    @Transactional
    public void updateChannelWhitelist(Long channelId, Long targetUserId, boolean add, Long operatorId) {
        CommunityChannel channel = channelMapper.selectById(channelId);
        if (channel == null)
            throw new RuntimeException("频道不存在");

        if (!isAdmin(operatorId) && !operatorId.equals(channel.getOwnerId())) {
            throw new RuntimeException("无权管理白名单");
        }

        if (add) {
            channel.setIsRestricted(true);
            channelMapper.updateById(channel);

            Long count = whitelistMapper.selectCount(new LambdaQueryWrapper<CommunityChannelWhitelist>()
                    .eq(CommunityChannelWhitelist::getChannelId, channelId)
                    .eq(CommunityChannelWhitelist::getUserId, targetUserId));
            if (count == 0) {
                CommunityChannelWhitelist whitelist = new CommunityChannelWhitelist();
                whitelist.setChannelId(channelId);
                whitelist.setUserId(targetUserId);
                whitelistMapper.insert(whitelist);
            }
        } else {
            whitelistMapper.delete(new LambdaQueryWrapper<CommunityChannelWhitelist>()
                    .eq(CommunityChannelWhitelist::getChannelId, channelId)
                    .eq(CommunityChannelWhitelist::getUserId, targetUserId));

            // Check if empty, maybe disable restriction? keeping simple for now.
        }
    }

    @Override
    public List<Map<String, Object>> getPosts(Long channelId) {
        List<CommunityPost> posts = postMapper.selectList(new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getChannelId, channelId)
                .orderByDesc(CommunityPost::getLastReplyAt));

        List<Long> authorIds = posts.stream().map(CommunityPost::getAuthorId).collect(Collectors.toList());
        Map<Long, String> userNames = getUserNames(authorIds);

        return posts.stream().map(post -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", post.getId());
            map.put("title", post.getTitle());
            map.put("content", post.getContent()); // Maybe truncate?
            map.put("authorName", userNames.getOrDefault(post.getAuthorId(), "Unknown"));
            map.put("createdAt", post.getCreatedAt());
            map.put("lastReplyAt", post.getLastReplyAt());
            map.put("upvotes", post.getUpvotes() == null ? 0 : post.getUpvotes());
            map.put("downvotes", post.getDownvotes() == null ? 0 : post.getDownvotes());
            return map;
        }).collect(Collectors.toList());
    }

    private Map<Long, String> getUserNames(List<Long> userIds) {
        if (userIds.isEmpty())
            return new HashMap<>();
        List<UserAccount> users = userAccountService.listByIds(userIds);
        return users.stream().collect(Collectors.toMap(UserAccount::getId, UserAccount::getUsername));
    }

    @Override
    public Map<String, Object> getPostDetail(Long postId, Long userId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null)
            throw new RuntimeException("帖子不存在");

        Map<String, Object> result = new HashMap<>();
        result.put("post", post);
        result.put("authorName", userAccountService.getById(post.getAuthorId()).getUsername());

        // Get replies
        List<CommunityReply> replies = replyMapper.selectList(new LambdaQueryWrapper<CommunityReply>()
                .eq(CommunityReply::getPostId, postId)
                .orderByAsc(CommunityReply::getCreatedAt));

        // Build reply tree
        List<Map<String, Object>> replyTree = buildReplyTree(replies);
        result.put("replies", replyTree);

        // Get user vote status
        if (userId != null) {
            CommunityVote vote = voteMapper.selectOne(new LambdaQueryWrapper<CommunityVote>()
                    .eq(CommunityVote::getUserId, userId)
                    .eq(CommunityVote::getTargetType, "POST")
                    .eq(CommunityVote::getTargetId, postId));
            result.put("userVote", vote != null ? vote.getVoteType() : 0);
            
            // Get user reply votes
            List<Long> replyIds = replies.stream().map(CommunityReply::getId).collect(java.util.stream.Collectors.toList());
            if (!replyIds.isEmpty()) {
                List<CommunityVote> replyVotes = voteMapper.selectList(new LambdaQueryWrapper<CommunityVote>()
                        .eq(CommunityVote::getUserId, userId)
                        .eq(CommunityVote::getTargetType, "REPLY")
                        .in(CommunityVote::getTargetId, replyIds));
                Map<Long, Integer> userReplyVotes = new HashMap<>();
                for (CommunityVote v : replyVotes) {
                    userReplyVotes.put(v.getTargetId(), v.getVoteType());
                }
                result.put("userReplyVotes", userReplyVotes);
            }
        }

        return result;
    }

    private List<Map<String, Object>> buildReplyTree(List<CommunityReply> replies) {
        Map<Long, Map<String, Object>> replyMap = new HashMap<>();
        List<Map<String, Object>> roots = new ArrayList<>();

        // First pass: create maps and get user names
        List<Long> authorIds = replies.stream().map(CommunityReply::getAuthorId).collect(Collectors.toList());
        Map<Long, String> userNames = getUserNames(authorIds);

        for (CommunityReply reply : replies) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", reply.getId());
            map.put("content", reply.getContent());
            map.put("authorId", reply.getAuthorId());
            map.put("authorName", userNames.getOrDefault(reply.getAuthorId(), "Unknown"));
            map.put("createdAt", reply.getCreatedAt());
            map.put("upvotes", reply.getUpvotes() == null ? 0 : reply.getUpvotes());
            map.put("downvotes", reply.getDownvotes() == null ? 0 : reply.getDownvotes());
            map.put("parentId", reply.getParentId());
            map.put("children", new ArrayList<Map<String, Object>>());

            replyMap.put(reply.getId(), map);
        }

        // Second pass: build tree
        for (CommunityReply reply : replies) {
            Map<String, Object> node = replyMap.get(reply.getId());
            if (reply.getParentId() == null) {
                roots.add(node);
            } else {
                Map<String, Object> parent = replyMap.get(reply.getParentId());
                if (parent != null) {
                    ((List<Map<String, Object>>) parent.get("children")).add(node);
                } else {
                    // Parent might be deleted or missing, treat as root? or ignore?
                    // For now, treat as root to be safe
                    roots.add(node);
                }
            }
        }

        return roots;
    }

    @Override
    @Transactional
    public void createPost(Long channelId, String title, String content, Long userId) {
        CommunityChannel channel = channelMapper.selectById(channelId);
        if (channel == null)
            throw new RuntimeException("频道不存在");

        checkBan(channelId, userId);
        checkWhitelist(channel, userId);

        CommunityPost post = new CommunityPost();
        post.setChannelId(channelId);
        post.setAuthorId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedAt(OffsetDateTime.now());
        post.setLastReplyAt(OffsetDateTime.now());
        post.setUpvotes(0);
        post.setDownvotes(0);

        postMapper.insert(post);

        // Update channel activity and heat
        channel.setLastActivityAt(OffsetDateTime.now());
        channel.setHeat((channel.getHeat() == null ? 0 : channel.getHeat()) + 10);
        updateChannelLevel(channel);
        channelMapper.updateById(channel);
    }

    @Override
    @Transactional
    public void createReply(Long postId, Long parentId, String content, Long userId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null)
            throw new RuntimeException("帖子不存在");

        checkBan(post.getChannelId(), userId);
        CommunityChannel channel = channelMapper.selectById(post.getChannelId());
        checkWhitelist(channel, userId);

        CommunityReply reply = new CommunityReply();
        reply.setPostId(postId);
        reply.setParentId(parentId);
        reply.setAuthorId(userId);
        reply.setContent(content);
        reply.setCreatedAt(OffsetDateTime.now());
        reply.setUpvotes(0);
        reply.setDownvotes(0);

        replyMapper.insert(reply);

        // Update post last reply time
        post.setLastReplyAt(OffsetDateTime.now());
        postMapper.updateById(post);

        // Update channel activity and heat
        channel.setLastActivityAt(OffsetDateTime.now());
        channel.setHeat((channel.getHeat() == null ? 0 : channel.getHeat()) + 2);
        updateChannelLevel(channel);
        channelMapper.updateById(channel);
    }

    @Override
    @Transactional
    public void deleteReply(Long replyId, Long userId) {
        CommunityReply reply = replyMapper.selectById(replyId);
        if (reply == null)
            return;

        CommunityPost post = postMapper.selectById(reply.getPostId());
        CommunityChannel channel = channelMapper.selectById(post.getChannelId());

        if (!isAdmin(userId) && !userId.equals(channel.getOwnerId())) {
            throw new RuntimeException("无权删除回复");
        }

        // Simply delete the reply since we don't have soft delete
        replyMapper.deleteById(replyId);
    }

    @Override
    @Transactional
    public void vote(String targetType, Long targetId, int voteType, Long userId) {
        // Check if already voted
        CommunityVote existingVote = voteMapper.selectOne(new LambdaQueryWrapper<CommunityVote>()
                .eq(CommunityVote::getUserId, userId)
                .eq(CommunityVote::getTargetType, targetType)
                .eq(CommunityVote::getTargetId, targetId));

        if (existingVote != null) {
            if (existingVote.getVoteType() == voteType) {
                // Cancel vote
                voteMapper.deleteById(existingVote.getId());
                adjustVotes(targetType, targetId, voteType, 0);
            } else {
                // Change vote
                int oldVote = existingVote.getVoteType();
                existingVote.setVoteType(voteType);
                voteMapper.updateById(existingVote);
                adjustVotes(targetType, targetId, oldVote, voteType);
            }
        } else {
            // New vote
            CommunityVote vote = new CommunityVote();
            vote.setUserId(userId);
            vote.setTargetType(targetType);
            vote.setTargetId(targetId);
            vote.setVoteType(voteType);
            voteMapper.insert(vote);
            adjustVotes(targetType, targetId, 0, voteType);

            // Update heat
            if ("POST".equals(targetType)) {
                CommunityPost post = postMapper.selectById(targetId);
                CommunityChannel channel = channelMapper.selectById(post.getChannelId());
                channel.setHeat((channel.getHeat() == null ? 0 : channel.getHeat()) + 1);
                updateChannelLevel(channel);
                channelMapper.updateById(channel);
            }
        }
    }

    private void adjustVotes(String targetType, Long targetId, int oldVote, int newVote) {
        if ("POST".equals(targetType)) {
            CommunityPost post = postMapper.selectById(targetId);
            if (oldVote == 1)
                post.setUpvotes(Math.max(0, (post.getUpvotes() == null ? 0 : post.getUpvotes()) - 1));
            if (oldVote == -1)
                post.setDownvotes(Math.max(0, (post.getDownvotes() == null ? 0 : post.getDownvotes()) - 1));

            if (newVote == 1)
                post.setUpvotes((post.getUpvotes() == null ? 0 : post.getUpvotes()) + 1);
            if (newVote == -1)
                post.setDownvotes((post.getDownvotes() == null ? 0 : post.getDownvotes()) + 1);

            postMapper.updateById(post);
        } else {
            CommunityReply reply = replyMapper.selectById(targetId);
            if (oldVote == 1)
                reply.setUpvotes(Math.max(0, (reply.getUpvotes() == null ? 0 : reply.getUpvotes()) - 1));
            if (oldVote == -1)
                reply.setDownvotes(Math.max(0, (reply.getDownvotes() == null ? 0 : reply.getDownvotes()) - 1));

            if (newVote == 1)
                reply.setUpvotes((reply.getUpvotes() == null ? 0 : reply.getUpvotes()) + 1);
            if (newVote == -1)
                reply.setDownvotes((reply.getDownvotes() == null ? 0 : reply.getDownvotes()) + 1);

            replyMapper.updateById(reply);
        }
    }

    @Override
    @Transactional
    public void banUser(Long channelId, Long targetUserId, int durationType, Long operatorId) {
        CommunityChannel channel = channelMapper.selectById(channelId);
        if (channel == null)
            throw new RuntimeException("频道不存在");

        if (!isAdmin(operatorId) && !operatorId.equals(channel.getOwnerId())) {
            throw new RuntimeException("无权禁言用户");
        }

        OffsetDateTime endTime = OffsetDateTime.now();
        switch (durationType) {
            case 1:
                endTime = endTime.plusHours(1);
                break;
            case 2:
                endTime = endTime.plusDays(1);
                break;
            case 3:
                endTime = endTime.plusWeeks(1);
                break;
            case 4:
                endTime = endTime.plusMonths(1);
                break;
            default:
                throw new RuntimeException("无效的禁言时长");
        }

        CommunityBan ban = new CommunityBan();
        ban.setChannelId(channelId);
        ban.setUserId(targetUserId);
        ban.setCreatedAt(OffsetDateTime.now());
        ban.setEndTime(endTime);

        banMapper.insert(ban);
    }

    private void updateChannelLevel(CommunityChannel channel) {
        long heat = channel.getHeat() == null ? 0 : channel.getHeat();
        // Simple level formula: level = 1 + heat / 100
        channel.setLevel(1 + (int) (heat / 100));
    }
}
