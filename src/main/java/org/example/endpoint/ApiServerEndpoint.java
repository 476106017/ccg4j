package org.example.endpoint;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.card.Card;
import org.example.card.ccg.neutral.ThePlayer;
import org.example.endpoint.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.stream.Collectors;
import org.example.auth.SessionConstants;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.example.game.PlayerInfo;
import org.example.system.Database;
import org.example.system.GsonConfig;
import org.example.system.WebSocketConfig;
import org.example.system.WebSocketConfigurator;
import org.example.system.util.Msg;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.example.system.Database.*;

@Slf4j
@ServerEndpoint(value = "/api/{name}", encoders = {
        GsonConfig.MyEncoder.class }, configurator = WebSocketConfigurator.CustomSpringConfigurator.class)
@Service
@DependsOn({ "chatHandler", "deckEditHandler", "gameHandler", "matchHandler" })
public class ApiServerEndpoint {

    @Autowired
    ChatHandler chatHandler;
    @Autowired
    DeckEditHandler deckEditHandler;
    @Autowired
    GameHandler gameHandler;
    @Autowired
    MatchHandler matchHandler;
    @Autowired
    CardSearchHandler cardSearchHandler;
    @Autowired
    Gson gson;

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) throws IOException {
        // handle open event
        final String name = session.getPathParameters().get("name");
        log.info("=== WebSocket连接开始 ===");
        log.info("Session ID: {}, Username: {}", session.getId(), name);

        if (Strings.isBlank(name) || userNames.containsValue(name)) {
            log.warn("用户名无效或已被使用 - Name: {}, IsBlank: {}, AlreadyExists: {}",
                    name, Strings.isBlank(name), userNames.containsValue(name));
            Msg.send(session, "用户名无法使用！");
            session.close();
            return;
        }
        session.getUserProperties().put("name", name);
        userNames.put(session, name);
        log.info("用户名已注册 - Session: {}, Name: {}", session.getId(), name);

        // Get HttpSession from config
        HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());
        log.info("HttpSession检查 - IsNull: {}", httpSession == null);

        if (httpSession != null) {
            Long userId = (Long) httpSession.getAttribute(SessionConstants.SESSION_USER_ID);
            log.info("从HttpSession获取UserId - SessionId: {}, UserId: {}", httpSession.getId(), userId);

            if (userId != null) {
                sessionUserIds.put(session, userId);
                // Record Login Activity
                org.example.user.service.UserAccountService userService = org.example.system.ApplicationContextHelper
                        .getBean(org.example.user.service.UserAccountService.class);
                userService.recordLogin(userId);

                log.info("用户认证成功 - WebSocketSession: {}, UserId: {}, Username: {}",
                        session.getId(), userId, name);
            } else {
                log.warn("HttpSession中没有UserId - HttpSessionId: {}, Username: {}",
                        httpSession.getId(), name);
            }
        } else {
            log.warn("无法获取HttpSession - Username: {}, 用户未登录或Session已过期", name);
        }

        // region
        PlayerDeck playerDeck = new PlayerDeck();
        playerDeck.setLeaderClass(ThePlayer.class);

        Set<Class<? extends Card>> subTypesOf = new Reflections(new ConfigurationBuilder()
                .filterInputsBy(s -> !s.contains("morecard"))
                .forPackage("org.example.card"))
                .getSubTypesOf(Card.class);
        // 移除不符合的卡牌类型
        subTypesOf.removeIf(aClass -> {
            int modifiers = aClass.getModifiers();
            return Modifier.isAbstract(modifiers) || Modifier.isStatic(modifiers);
        });
        // 随机取30张
        List<String> cardCodes = subTypesOf.stream()
                .map(Class::getName)
                .collect(Collectors.toList());
        Collections.shuffle(cardCodes);
        playerDeck.getActiveDeck().addAll(cardCodes.subList(0, Math.min(30, cardCodes.size())));
        userDecks.put(session, playerDeck);
        // endregion
        Msg.send(session, name + "登录成功！");
        final int size = userNames.size();
        WebSocketConfig.broadcast("【全体】有玩家登陆了游戏！当前在线：" + size + "人");

        log.info("=== WebSocket连接完成 ===");
        log.info("当前在线人数: {}, 已认证用户数: {}", userNames.size(), sessionUserIds.size());
    }

    @OnClose
    public void onClose(Session session) {
        // handle close event
        String name = userNames.get(session);
        userNames.remove(session);
        sessionUserIds.remove(session);
        final int size = userNames.size();
        WebSocketConfig.broadcast("【全体】有玩家退出了游戏！当前在线：" + size + "人");
        String room = userRoom.get(session);
        if (room == null)
            return;

        GameInfo info = roomGame.get(room);
        if (info != null) {
            PlayerInfo player = info.playerBySession(session);
            PlayerInfo enemy = info.anotherPlayerBySession(session);
            info.msg(player.getName() + "已断开连接！");
            info.gameset(enemy);
            return;
        }
        // 释放资源
        roomGame.remove(room);
        userRoom.remove(session);
        if (session == waitUser || room.equals(waitRoom)) {
            waitRoom = "";
            waitUser = null;
            WebSocketConfig.broadcast("【全体】匹配中的玩家已经退出了！");
        }
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        // handle message event
        try {

            String trimmed = msg.trim();
            String param = "";
            String command = trimmed;

            // 检查是否是JSON格式的消息
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                Type type = new TypeToken<Map<String, Object>>() {
                }.getType();
                Map<String, Object> jsonMsg = gson.fromJson(trimmed, type);
                String messageType = (String) jsonMsg.get("type");
                Map<String, Object> data = (Map<String, Object>) jsonMsg.get("data");

                switch (messageType) {
                    case "search_cards" -> cardSearchHandler.searchCards(session, data);
                    case "update_deck" -> deckEditHandler.setdeck(session, gson.toJson(data));
                    case "chat_global" -> chatHandler.handleGlobalChat(session, (String) data.get("content"));
                    case "chat_private" -> {
                        Double targetId = (Double) data.get("targetId");
                        chatHandler.handlePrivateChat(session, targetId.longValue(), (String) data.get("content"));
                    }
                    default -> Msg.send(session, "不支持的JSON消息类型！");
                }
                return;
            }

            int delimiterIndex = trimmed.indexOf("::");
            int delimiterLength = 2;
            if (delimiterIndex < 0) {
                delimiterIndex = trimmed.indexOf(':');
                delimiterLength = 1;
            }
            if (delimiterIndex >= 0) {
                command = trimmed.substring(0, delimiterIndex);
                if (delimiterIndex + delimiterLength < trimmed.length()) {
                    param = trimmed.substring(delimiterIndex + delimiterLength);
                }
            }

            // 兼容：如果没有使用"::"分隔，但命令包含空格（如 "discover 1"），把空格后的部分视为参数
            if (command.contains(" ")) {
                String[] parts = command.split("\\s+", 2);
                command = parts[0];
                if (param.isBlank() && parts.length > 1) {
                    param = parts[1];
                }
            }

            switch (command) {
                case "joinRoom" -> matchHandler.joinRoom(session, param);
                case "leave" -> matchHandler.leave(session);
                case "cancelAISearch" -> matchHandler.cancelBorderlandAISearch(session);

                case "deck" -> deckEditHandler.deck(session);
                case "usedeck" -> deckEditHandler.usedeck(session, param);
                case "setdeck" -> deckEditHandler.setdeck(session, param);
                case "chat" -> chatHandler.chat(session, param);
                case "challenge" -> {
                    try {
                        Long targetId = Long.parseLong(param);
                        matchHandler.handleChallengeRequest(session, targetId);
                    } catch (NumberFormatException e) {
                        Msg.send(session, "无效的用户ID");
                    }
                }

                case "swap" -> gameHandler.swap(session, param);
                case "play" -> gameHandler.play(session, param);
                case "attack" -> gameHandler.attack(session, param);
                case "discover" -> gameHandler.discover(session, param);
                case "skill" -> gameHandler.skill(session, param);

                case "end" -> gameHandler.end(session, param);
                case "test" -> gameHandler.test(session);
                default -> Msg.send(session, "不存在的指令！");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @OnError
    public void onError(Throwable t) {
        // handle error event
    }

}
