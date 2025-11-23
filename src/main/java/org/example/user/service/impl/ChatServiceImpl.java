package org.example.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.user.entity.ChatMessage;
import org.example.user.mapper.ChatMapper;
import org.example.user.service.ChatService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, ChatMessage> implements ChatService {

    @Override
    public void saveMessage(Long senderId, Long receiverId, String content, String type) {
        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(type);
        message.setCreatedAt(OffsetDateTime.now());
        save(message);
    }

    @Override
    public List<ChatMessage> getRecentGlobalMessages(int limit) {
        return lambdaQuery()
                .eq(ChatMessage::getType, "GLOBAL")
                .orderByDesc(ChatMessage::getCreatedAt)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    public List<ChatMessage> getRecentGlobalMessagesWithTimeLimit(int hours, int limit) {
        OffsetDateTime timeLimit = OffsetDateTime.now().minusHours(hours);
        return lambdaQuery()
                .eq(ChatMessage::getType, "GLOBAL")
                .ge(ChatMessage::getCreatedAt, timeLimit)
                .orderByDesc(ChatMessage::getCreatedAt)
                .last("LIMIT " + limit)
                .list();
    }

    @Override
    public List<ChatMessage> getPrivateMessages(Long userId, Long friendId, int limit) {
        return lambdaQuery()
                .eq(ChatMessage::getType, "PRIVATE")
                .and(wrapper -> wrapper
                        .eq(ChatMessage::getSenderId, userId).eq(ChatMessage::getReceiverId, friendId)
                        .or()
                        .eq(ChatMessage::getSenderId, friendId).eq(ChatMessage::getReceiverId, userId))
                .orderByDesc(ChatMessage::getCreatedAt)
                .last("LIMIT " + limit)
                .list();
    }
}
