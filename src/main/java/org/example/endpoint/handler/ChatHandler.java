package org.example.endpoint.handler;

import com.google.gson.Gson;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.constant.ChatPreset;
import org.example.system.util.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static org.example.system.Database.userNames;
import static org.example.system.Database.userRoom;

@Service
@Slf4j
public class ChatHandler {

    @Autowired
    Gson gson;

    @Autowired
    private org.example.user.service.ChatService chatService;
    @Autowired
    private org.example.user.service.FriendService friendService;

    /**
     * 发送房间预置消息
     */
    public void chat(Session client, String data) throws IOException {
        final String room = userRoom.get(client);
        if (Strings.isBlank(room)) {
            Msg.send(client, "请先加入房间！");
            return;
        }
        if (data.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("可以使用的预置信息：\n");
            for (ChatPreset preset : ChatPreset.values()) {
                sb.append(preset.getId()).append("\t")
                        .append(preset.getCh()).append("\n");
            }
            Msg.send(client, sb.toString());
            return;
        }
        String name = userNames.get(client);

        Integer id = -1;
        try {
            id = Integer.valueOf(data);
        } catch (Exception e) {
            log.warn("聊天ID解析失败: {}", data);
        }

        for (ChatPreset preset : ChatPreset.values()) {
            if (id.equals(preset.getId())) {
                userRoom.forEach((k, v) -> {
                    if (room.equals(v)) {
                        Msg.send(k, "【房间】" + name + "：" + preset.getCh());
                    }
                });
                return;
            }
        }

        Msg.send(client, "输入序号错误！");
    }

    public void handleGlobalChat(Session client, String content) {
        String name = userNames.get(client);
        if (Strings.isBlank(name))
            return;

        Long userId = org.example.system.Database.sessionUserIds.get(client);
        if (userId != null) {
            chatService.saveMessage(userId, null, content, "GLOBAL");
        }

        // Send structured message
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("sender", name);
        data.put("senderId", userId); // Add senderId for block checking
        data.put("content", content);
        data.put("type", "GLOBAL");

        // Broadcast with block check
        userNames.keySet().forEach(userSession -> {
            Long receiverId = org.example.system.Database.sessionUserIds.get(userSession);
            // Check if receiver has blocked sender
            boolean isBlocked = false;
            if (receiverId != null && userId != null) {
                isBlocked = friendService.isBlocked(receiverId, userId);
            }

            if (isBlocked) {
                java.util.Map<String, Object> blockedData = new java.util.HashMap<>(data);
                blockedData.put("content", "【已屏蔽】");
                Msg.send(userSession, "chat_global", blockedData);
            } else {
                Msg.send(userSession, "chat_global", data);
            }
        });
    }

    public void handlePrivateChat(Session client, Long targetUserId, String content) {
        String name = userNames.get(client);
        Long userId = org.example.system.Database.sessionUserIds.get(client);

        if (userId == null) {
            Msg.send(client, "请先登录！");
            return;
        }

        // Check if target is online
        Session targetSession = null;
        for (java.util.Map.Entry<Session, Long> entry : org.example.system.Database.sessionUserIds.entrySet()) {
            if (entry.getValue().equals(targetUserId)) {
                targetSession = entry.getKey();
                break;
            }
        }

        if (targetSession == null) {
            Msg.send(client, "对方不在线！");
            return;
        }

        // Check if target blocked sender
        if (friendService.isBlocked(targetUserId, userId)) {
            Msg.send(client, "对方已屏蔽您的消息！");
            return;
        }

        chatService.saveMessage(userId, targetUserId, content, "PRIVATE");

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("sender", name);
        data.put("senderId", userId);
        data.put("content", content);
        data.put("type", "PRIVATE");

        Msg.send(targetSession, "chat_private", data);

        // Send confirmation to sender
        data.put("sender", "我");
        data.put("receiver", userNames.get(targetSession));
        Msg.send(client, "chat_private", data);
    }
}
