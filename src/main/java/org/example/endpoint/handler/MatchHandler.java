package org.example.endpoint.handler;

import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.card.Card;
import org.example.card.ccg.neutral.ThePlayer;
import org.example.card.dto.CardSummary;
import org.example.card.service.CardCatalogService;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.example.game.PlayerInfo;
import org.example.game.ai.AiRegistry;
import org.example.game.ai.BorderlandAiController;
import org.example.system.GameConfig;
import org.example.system.WebSocketConfig;
import org.example.system.util.Msg;
import org.example.user.entity.BorderlandVisa;
import org.example.user.service.BorderlandService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.example.system.Database.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MatchHandler {

    private static final String BORDERLAND_MODE = "borderland";
    private static final String BORDERLAND_AI_NAME = "弥留AI";

    private final BorderlandService borderlandService;
    private final CardCatalogService cardCatalogService;
    private final GameConfig gameConfig;
    private final org.example.user.service.BorderlandBattleLogService battleLogService;

    // 弥留之国AI搜寻等待池（10秒暴露期，可被猎杀）
    private final ConcurrentHashMap<Session, PlayerDeckWithTask> aiWaitingPool = new ConcurrentHashMap<>();
    private final ScheduledExecutorService matchScheduler = Executors.newScheduledThreadPool(4);

    // 内部类：存储卡组和定时任务
    private static class PlayerDeckWithTask {
        PlayerDeck deck;
        ScheduledFuture<?> task;

        PlayerDeckWithTask(PlayerDeck deck, ScheduledFuture<?> task) {
            this.deck = deck;
            this.task = task;
        }
    }

    /**
     * 加入房间进行匹配
     */
    public void joinRoom(Session client, String mode) throws IOException {
        String normalizedMode = mode == null ? "" : mode.trim().toLowerCase();
        if ("borderland-ai".equals(normalizedMode)) {
            startBorderlandAIDirectly(client);
            return;
        }
        if ("borderland-pvp".equals(normalizedMode)) {
            startBorderlandPvpMatch(client);
            return;
        }
        if (BORDERLAND_MODE.equals(normalizedMode)) {
            // 兼容旧的调用，默认AI模式
            startBorderlandAIDirectly(client);
            return;
        }
        joinNormalRoom(client);
    }

    private void joinNormalRoom(Session client) throws IOException {
        String room = userRoom.get(client);
        if (room != null) {
            Msg.warn(client, "请不要重复进入房间！");
            return;
        }

        if (waitRoom.isBlank()) {
            waitUser = client;
            waitRoom = UUID.randomUUID().toString();
            userRoom.put(client, waitRoom);
            Msg.send(client, "waitRoom", waitRoom);

            WebSocketConfig.broadcast("【全体】有人正在匹配对战，点击匹配以尝试加入该对战！");
        } else {
            userRoom.put(client, waitRoom);
            Msg.send(client, "进入房间：" + waitRoom);
            Msg.send(client, "匹配成功【" + userNames.get(client) + "】vs【" + userNames.get(waitUser) + "】");
            Msg.send(waitUser, "匹配成功【" + userNames.get(waitUser) + "】vs【" + userNames.get(client) + "】");

            WebSocketConfig.broadcast("【全体】一场对战已经匹配成功！");

            GameInfo info = new GameInfo(waitRoom);
            info.zeroTurn(waitUser, client);
            roomGame.put(waitRoom, info);

            waitRoom = "";
            waitUser = null;
        }
    }

    /**
     * 搜寻AI - 进入10秒暴露期，可被猎杀
     */
    private void startBorderlandAIDirectly(Session client) {
        log.info("=== 开始弥留之国AI匹配调试 ===");
        log.info("Session ID: {}", client.getId());
        log.info("Session isOpen: {}", client.isOpen());
        
        String room = userRoom.get(client);
        if (room != null) {
            log.warn("玩家重复进入房间 - Session: {}, Room: {}", client.getId(), room);
            Msg.warn(client, "请不要重复进入房间！");
            return;
        }
        
        // 详细日志：检查用户认证
        Long userId = sessionUserIds.get(client);
        String username = userNames.get(client);
        log.info("用户认证检查 - Session: {}, UserName: {}, UserId: {}", client.getId(), username, userId);
        log.info("所有已认证Session: {}", sessionUserIds.keySet().stream()
            .map(s -> String.format("Session[%s]->User[%s]", s.getId(), sessionUserIds.get(s)))
            .collect(java.util.stream.Collectors.joining(", ")));
        
        if (userId == null) {
            log.error("用户ID为空！尝试从用户名恢复... UserName: {}", username);
            Msg.warn(client, "弥留之国模式需要先登录账号！当前Session未关联用户ID，请刷新页面重新登录。");
            return;
        }
        
        log.info("开始获取签证状态 - UserId: {}", userId);
        BorderlandVisa visa = borderlandService.getVisaStatus(userId);
        if (visa == null) {
            log.warn("未找到签证 - UserId: {}", userId);
            Msg.warn(client, "当前没有有效的弥留之国签证，请先在页面上办理。");
            return;
        }
        
        log.info("签证信息 - UserId: {}, Status: {}, DaysRemaining: {}", userId, visa.getStatus(), visa.getDaysRemaining());
        
        if (!"ACTIVE".equalsIgnoreCase(visa.getStatus())) {
            log.warn("签证状态不是ACTIVE - UserId: {}, Status: {}", userId, visa.getStatus());
            Msg.warn(client, "当前没有有效的弥留之国签证，请先在页面上办理。");
            return;
        }
        
        PlayerDeck playerDeck = buildDeckFromVisa(visa);
        log.info("卡组构建完成 - UserId: {}, DeckSize: {}", userId, playerDeck.getActiveDeck().size());
        
        if (playerDeck.getActiveDeck().isEmpty()) {
            log.warn("签证卡组为空 - UserId: {}", userId);
            Msg.warn(client, "签证卡组为空，无法开始战斗。");
            return;
        }

        // 进入10秒暴露期，期间可被搜寻玩家入侵
        Msg.send(client, "borderland-ai-waiting", String.valueOf(gameConfig.getAiMatchWaitSeconds()));
        log.info("玩家 {} ({}) 开始搜寻AI，进入{}秒暴露期", username, userId, gameConfig.getAiMatchWaitSeconds());

        ScheduledFuture<?> task = matchScheduler.schedule(() -> {
            // 10秒后如果没被猎杀，开始AI战斗
            PlayerDeckWithTask removed = aiWaitingPool.remove(client);
            if (removed != null) {
                log.info("暴露期结束，开始AI战斗 - UserId: {}", userId);
                startBorderlandAI(client, playerDeck);
            } else {
                log.warn("玩家已被移除等待池 - UserId: {}", userId);
            }
        }, gameConfig.getAiMatchWaitSeconds(), TimeUnit.SECONDS);

        aiWaitingPool.put(client, new PlayerDeckWithTask(playerDeck, task));
        log.info("玩家已加入等待池 - UserId: {}, 当前等待池大小: {}", userId, aiWaitingPool.size());
    }

    /**
     * 取消搜寻AI
     */
    public void cancelBorderlandAISearch(Session client) {
        PlayerDeckWithTask removed = aiWaitingPool.remove(client);
        if (removed != null) {
            // 取消定时任务
            removed.task.cancel(false);
            Msg.send(client, "borderland-ai-cancelled", "已取消搜寻AI");
            log.info("玩家 {} 取消了搜寻AI", sessionUserIds.get(client));
        } else {
            Msg.warn(client, "当前没有正在进行的AI搜寻");
        }
    }

    /**
     * 搜寻玩家 - 主动猎杀正在搜寻AI的玩家
     */
    private void startBorderlandPvpMatch(Session client) {
        String room = userRoom.get(client);
        if (room != null) {
            Msg.warn(client, "请不要重复进入房间！");
            return;
        }
        Long userId = sessionUserIds.get(client);
        if (userId == null) {
            Msg.warn(client, "弥留之国模式需要先登录账号！");
            return;
        }
        BorderlandVisa visa = borderlandService.getVisaStatus(userId);
        if (visa == null || !"ACTIVE".equalsIgnoreCase(visa.getStatus())) {
            Msg.warn(client, "当前没有有效的弥留之国签证，请先在页面上办理。");
            return;
        }
        PlayerDeck playerDeck = buildDeckFromVisa(visa);
        if (playerDeck.getActiveDeck().isEmpty()) {
            Msg.warn(client, "签证卡组为空，无法开始战斗。");
            return;
        }

        // 搜寻等待中的AI玩家进行入侵
        Session target = null;
        PlayerDeck targetDeck = null;

        for (Session waiting : aiWaitingPool.keySet()) {
            if (!waiting.equals(client) && waiting.isOpen()) {
                PlayerDeckWithTask removed = aiWaitingPool.remove(waiting);
                if (removed != null) {
                    // 取消AI任务
                    removed.task.cancel(false);
                    target = waiting;
                    targetDeck = removed.deck;
                    log.info("玩家 {} 入侵了正在搜寻AI的玩家 {}", userId, sessionUserIds.get(target));
                    break;
                }
            }
        }

        if (target != null) {
            // 成功入侵，开始PVP
            startBorderlandPvP(client, target, playerDeck, targetDeck, true);
        } else {
            // 没有找到目标
            Msg.warn(client, "当前没有正在搜寻AI的玩家，无法进行猎杀！");
        }
    }

    /**
     * 开始弥留之国PVP战斗
     * @param isInvasion 是否为入侵模式（猎杀）
     */
    private void startBorderlandPvP(Session hunter, Session target, PlayerDeck hunterDeck, PlayerDeck targetDeck, boolean isInvasion) {
        String borderlandRoom = "borderland-pvp-" + UUID.randomUUID();
        GameInfo info = new GameInfo(borderlandRoom);

        info.zeroTurnWithDecks(
            target, targetDeck, userNames.get(target),
            hunter, hunterDeck, userNames.get(hunter)
        );

        userRoom.put(target, borderlandRoom);
        userRoom.put(hunter, borderlandRoom);
        roomGame.put(borderlandRoom, info);
        // 初始化房间计时器（必须在游戏开始前）
        roomSchedule.put(borderlandRoom, java.util.concurrent.Executors.newScheduledThreadPool(1));

        if (isInvasion) {
            // 入侵模式：猎人入侵目标
            Msg.send(target, "borderland-invaded", "⚠️ 警告：被猎杀者入侵！你必须在这场战斗中获胜！");
            Msg.send(hunter, "alert", "🎯 成功猎杀目标！");

            // 保存战斗记录到数据库
            org.example.game.BorderlandBattleLog matchLog = new org.example.game.BorderlandBattleLog(
                "match",
                userNames.get(target),
                userNames.get(hunter) + "(入侵)",
                null,
                null
            );
            battleLogService.save(matchLog);

            // 广播给所有在线玩家
            broadcastBattleLog(matchLog);
        } else {
            // 普通PVP
            Msg.send(target, "alert", "匹配成功！开始PVP对战！");
            Msg.send(hunter, "alert", "匹配成功！开始PVP对战！");

            // 保存战斗记录到数据库
            org.example.game.BorderlandBattleLog matchLog = new org.example.game.BorderlandBattleLog(
                "match",
                userNames.get(target),
                userNames.get(hunter),
                null,
                null
            );
            battleLogService.save(matchLog);

            // 广播给所有在线玩家
            broadcastBattleLog(matchLog);
        }

        Msg.send(target, "匹配成功【" + userNames.get(target) + "】vs【" + userNames.get(hunter) + "】");
        Msg.send(hunter, "匹配成功【" + userNames.get(hunter) + "】vs【" + userNames.get(target) + "】");

        log.info("弥留之国PVP开始: {} vs {} (入侵模式: {})", userNames.get(target), userNames.get(hunter), isInvasion);
    }

    /**
     * 开始弥留之国AI战斗（无人入侵）
     */
    private void startBorderlandAI(Session client, PlayerDeck playerDeck) {
        PlayerDeck aiDeck = buildRandomDeck(Math.max(playerDeck.getActiveDeck().size(), 40));
        if (aiDeck.getActiveDeck().isEmpty()) {
            Msg.warn(client, "卡池暂不可用，无法生成AI卡组。");
            return;
        }

        String borderlandRoom = "borderland-ai-" + UUID.randomUUID();
        GameInfo info = new GameInfo(borderlandRoom);
        info.zeroTurnWithDecks(client, playerDeck, userNames.get(client),
            null, aiDeck, BORDERLAND_AI_NAME);

        PlayerInfo aiPlayer = info.anotherPlayerBySession(client);
        aiPlayer.setStep(0);
        aiPlayer.setAiControlled(true);
        aiPlayer.setName(BORDERLAND_AI_NAME);

        // 保存AI的初始卡组代码列表，用于结算时获取
        List<String> aiDeckCodes = aiDeck.getActiveDeck().stream()
            .map(Class::getName)
            .collect(java.util.stream.Collectors.toList());
        info.setAiInitialDeckCodes(aiDeckCodes);

        userRoom.put(client, borderlandRoom);
        roomGame.put(borderlandRoom, info);
        // 初始化房间计时器（必须在startTurn之前）
        roomSchedule.put(borderlandRoom, java.util.concurrent.Executors.newScheduledThreadPool(1));
        AiRegistry.register(info, new BorderlandAiController(BORDERLAND_AI_NAME));

        Msg.send(client, "匹配成功！你将与【" + BORDERLAND_AI_NAME + "】对战，请完成换牌。");

        // 保存AI匹配记录到数据库
        org.example.game.BorderlandBattleLog matchLog = new org.example.game.BorderlandBattleLog(
            "match",
            userNames.get(client),
            BORDERLAND_AI_NAME,
            null,
            null
        );
        battleLogService.save(matchLog);

        // 广播给所有在线玩家
        broadcastBattleLog(matchLog);

        log.info("玩家 {} 开始与AI战斗", sessionUserIds.get(client));
    }

    private PlayerDeck buildDeckFromVisa(BorderlandVisa visa) {
        PlayerDeck deck = new PlayerDeck();
        deck.setLeaderClass(ThePlayer.class);
        List<Class<? extends Card>> cards = new ArrayList<>();
        if (visa.getDeckData() == null || visa.getDeckData().isBlank()) {
            deck.setActiveDeck(cards);
            return deck;
        }
        Arrays.stream(visa.getDeckData().split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(this::safeCardClass)
            .filter(Objects::nonNull)
            .forEach(cards::add);
        deck.setActiveDeck(cards);
        return deck;
    }

    private PlayerDeck buildRandomDeck(int size) {
        PlayerDeck deck = new PlayerDeck();
        deck.setLeaderClass(ThePlayer.class);
        List<CardSummary> allCards = cardCatalogService.getAllCards();
        if (allCards.isEmpty()) {
            return deck;
        }
        List<Class<? extends Card>> classes = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < size; i++) {
            CardSummary summary = allCards.get(random.nextInt(allCards.size()));
            Class<? extends Card> clazz = safeCardClass(summary.getCode());
            if (clazz != null) {
                classes.add(clazz);
            }
        }
        deck.setActiveDeck(classes);
        return deck;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Card> safeCardClass(String code) {
        try {
            Class<?> clazz = Class.forName(code);
            if (Card.class.isAssignableFrom(clazz)) {
                return (Class<? extends Card>) clazz;
            }
        } catch (ClassNotFoundException e) {
            log.warn("Card class not found: {}", code);
        }
        return null;
    }

    public void leave(Session client) throws IOException {
        String room = userRoom.get(client);
        if(room==null){
            Msg.send(client,"你不在任何房间中");
            return;
        }
        GameInfo info = roomGame.get(room);
        if(info!=null){
            PlayerInfo player = info.playerBySession(client);
            PlayerInfo enemy = info.anotherPlayerBySession(client);
            info.msg(player.getName() + "离开了游戏！");
            info.gameset(enemy);
            return;
        }
        userRoom.remove(client);
        Msg.send(client,"离开房间成功");
        // release resources
        roomGame.remove(room);
        if(client.equals(waitUser) || room.equals(waitRoom) ){
            waitRoom = "";
            waitUser = null;
            WebSocketConfig.broadcast("【全体】匹配中的玩家已经退出了！");
        }
        // exit room
    }

    /**
     * 广播战斗记录给所有在线玩家
     */
    private void broadcastBattleLog(org.example.game.BorderlandBattleLog msg) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("type", msg.getEventType());
        data.put("player1", msg.getPlayer1Name());
        data.put("player2", msg.getPlayer2Name());
        data.put("winner", msg.getWinnerName());
        data.put("timestamp", msg.getTimestamp().toString());
        data.put("punishmentSeconds", msg.getPunishmentSeconds());

        // 广播给所有玩家
        for (Session session : userNames.keySet()) {
            try {
                Msg.send(session, "borderland-battle-log", data);
            } catch (Exception e) {
                log.warn("Failed to broadcast battle log to session", e);
            }
        }
    }
}
