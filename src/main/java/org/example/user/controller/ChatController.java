package org.example.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.system.util.Result;
import org.example.user.entity.ChatMessage;
import org.example.user.entity.UserAccount;
import org.example.user.service.ChatService;
import org.example.user.service.UserAccountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserAccountService userAccountService;

    /**
     * 获取聊天历史记录
     * 返回最近100小时内的最多100条全局聊天记录
     */
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> getChatHistory() {
        // 获取最近100小时，最多100条消息
        List<ChatMessage> messages = chatService.getRecentGlobalMessagesWithTimeLimit(100, 100);

        // 反转列表，使最旧的消息在前
        Collections.reverse(messages);

        // 获取所有发送者ID
        Set<Long> senderIds = messages.stream()
                .map(ChatMessage::getSenderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 批量查询用户名
        Map<Long, String> userNames = new HashMap<>();
        if (!senderIds.isEmpty()) {
            List<UserAccount> users = userAccountService.listByIds(senderIds);
            for (UserAccount user : users) {
                userNames.put(user.getId(), user.getUsername());
            }
        }

        // 格式化消息
        List<Map<String, Object>> result = messages.stream()
                .map(msg -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("senderId", msg.getSenderId());
                    map.put("senderName", userNames.getOrDefault(msg.getSenderId(), "系统"));
                    map.put("content", msg.getContent());

                    // 格式化时间戳：当天只显示时分秒，非当天显示月日时分秒
                    String timestamp = "";
                    if (msg.getCreatedAt() != null) {
                        // Convert to system timezone (e.g., JST for Japan)
                        java.time.ZonedDateTime zonedTime = msg.getCreatedAt()
                                .atZoneSameInstant(java.time.ZoneId.systemDefault());

                        java.time.LocalDate today = java.time.LocalDate.now();
                        java.time.LocalDate msgDate = zonedTime.toLocalDate();

                        if (msgDate.equals(today)) {
                            // 当天：只显示时分秒
                            timestamp = zonedTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        } else {
                            // 非当天：显示月日时分秒
                            timestamp = zonedTime.format(DateTimeFormatter.ofPattern("MM月dd日 HH:mm:ss"));
                        }
                    }

                    map.put("timestamp", timestamp);
                    map.put("type", msg.getType());
                    return map;
                })
                .collect(Collectors.toList());

        return Result.success(result);
    }
}
