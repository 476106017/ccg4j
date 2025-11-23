package org.example.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.user.entity.ChatMessage;

import java.util.List;

public interface ChatService extends IService<ChatMessage> {
    void saveMessage(Long senderId, Long receiverId, String content, String type);

    List<ChatMessage> getRecentGlobalMessages(int limit);

    List<ChatMessage> getRecentGlobalMessagesWithTimeLimit(int hours, int limit);

    List<ChatMessage> getPrivateMessages(Long userId, Long friendId, int limit);
}
