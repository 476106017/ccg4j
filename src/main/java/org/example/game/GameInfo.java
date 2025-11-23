package org.example.game;

import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.example.card.*;
import org.example.constant.EffectTiming;
import org.example.game.ai.AiRegistry;
import org.example.system.GameConfig;
import org.example.system.util.CardPackage;
import org.example.system.util.Lists;
import org.example.system.util.Maps;
import org.example.system.util.Msg;
import org.example.system.util.SpringContext;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.PLAY_NUM;
import static org.example.constant.CounterKey.POISON;
import static org.example.system.Database.*;

@Slf4j
@Getter
@Setter
public class GameInfo implements Serializable {

    String room;

    // 连锁
    boolean canChain = true;
    int chainDeep = 3;
    boolean inSettle = false;
    int turn;
    int turnPlayer;
    int moreTurn = 0;// 追加回合
    boolean gameset = false;
    ScheduledFuture<?> rope;
    List<Damage> incommingDamages = new ArrayList<>();
    Map<Card, EventType> events = new HashMap<>();
    // 使用队列保证先入先出（FIFO）处理效果实例，处理时新产生的效果会继续入队并被处理
    Queue<Effect.EffectInstance> effectInstances = new LinkedList<>();

    // 弥留之国：存储AI的初始卡组代码（用于结算时获取）
    private transient List<String> aiInitialDeckCodes;

    // 对战统计信息
    private transient long battleStartTime; // 对战开始时间（毫秒）
    private transient int totalTurns = 0; // 总回合数
    private transient String battleMode; // 对战模式：normal, borderland
    private transient String endReason; // 结束原因
    private transient List<String> battleLog = new ArrayList<>(); // 对战日志

    public boolean hasEvent() {
        return !incommingDamages.isEmpty() || !events.isEmpty();
    }

    /**
     * 添加对战日志
     */
    public void addBattleLog(String msg) {
        String logEntry = String.format("[T%d] %s", turn, msg);
        battleLog.add(logEntry);
        // 同时输出到控制台
        log.info("游戏日志: {}", logEntry);
    }

    public void setCanChain(boolean canChain) {
        if (canChain)
            msg("本场游戏已启用连锁");
        else
            msg("本场游戏已禁用连锁");

        this.canChain = canChain;
    }

    PlayerInfo[] playerInfos;

    public GameInfo(String room) {
        this.room = room;
        this.turn = 1;
        this.turnPlayer = 0;
        this.playerInfos = new PlayerInfo[2];
        this.playerInfos[0] = new PlayerInfo(this, true);
        this.playerInfos[1] = new PlayerInfo(this, false);

    }

    public void resetGame() {
        msg("游戏重启！");
        AiRegistry.unregister(this);
        roomSchedule.get(getRoom()).shutdown();
        roomSchedule.remove(getRoom());
        rope.cancel(true);
        this.turn = 1;
        this.turnPlayer = 0;
        Session thisSession = thisPlayer().session;
        Session oppoSession = oppositePlayer().session;
        this.playerInfos = new PlayerInfo[2];
        this.playerInfos[0] = new PlayerInfo(this, true);
        this.playerInfos[1] = new PlayerInfo(this, false);
        zeroTurn(thisSession, oppoSession);
    }

    public void msg(String msg) {
        try {
            Msg.send(thisPlayer().getSession(), msg);
            Msg.send(oppositePlayer().getSession(), msg);
            // 同时记录到对战日志
            addBattleLog(msg);
            System.out.println(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void story(String msg) {
        try {
            Msg.story(thisPlayer().getSession(), msg);
            Msg.story(oppositePlayer().getSession(), msg);
        } catch (Exception ignored) {
        }
    }

    public void msgTo(Session session, String msg) {
        Msg.send(session, msg);
    }

    public void pushInfo() {
        final PlayerInfo thisPlayer = thisPlayer();
        thisPlayer.setDeckCount(thisPlayer.getDeck().size());
        final PlayerInfo oppositePlayer = oppositePlayer();
        oppositePlayer.setDeckCount(oppositePlayer.getDeck().size());
        // region 加载补充信息
        thisPlayer.getAreaAsCard().forEach(f -> f.setSubMarkStr(f.getSubMark()));
        thisPlayer.getHand().forEach(f -> f.setSubMarkStr(f.getSubMark()));
        oppositePlayer.getAreaAsCard().forEach(f -> f.setSubMarkStr(f.getSubMark()));
        // endregion 加载补充信息
        thisPlayer.getAreaAsCard().forEach(f -> f.setSubMarkStr(f.getSubMark()));
        thisPlayer.getAreaFollowsAsFollow().forEach(f -> {
            // 回合可攻击数没有打满
            final boolean notAttacked = f.getTurnAttack() < f.getTurnAttackMax();
            // 状态正常
            final boolean normalStatus = !f.hasKeyword("缴械") && !f.hasKeyword("眩晕") && !f.hasKeyword("冻结");
            final boolean canAttack = notAttacked && normalStatus &&
                    (f.getTurnAge() > 0 || f.hasKeyword("疾驰"));
            final boolean canDash = notAttacked && normalStatus &&
                    (f.getTurnAge() == 0 && !f.hasKeyword("疾驰") && f.hasKeyword("突进"));

            f.setCanAttack(canAttack);
            f.setCanDash(canDash);
        });

        thisPlayer.refreshLeaderStatuses();
        oppositePlayer.refreshLeaderStatuses();

        Msg.send(thisPlayer.getSession(), "battleInfo",
                Maps.newMap("me", thisPlayer, "enemy", oppositePlayer));
        Msg.send(oppositePlayer.getSession(), "battleInfo",
                Maps.newMap("me", oppositePlayer, "enemy", thisPlayer));
    }

    public void msgToThisPlayer(String msg) {
        Msg.send(thisPlayer().getSession(), msg);
    }

    public void msgToOppositePlayer(String msg) {
        Msg.send(oppositePlayer().getSession(), msg);
    }

    public void measureLeader() {
        if (thisPlayer().getHp() <= 0) {
            endReason = "hp_zero";
            addBattleLog(String.format("%s 生命值归零", thisPlayer().getName()));
            gameset(oppositePlayer());
        }
        if (oppositePlayer().getHp() <= 0) {
            endReason = "hp_zero";
            addBattleLog(String.format("%s 生命值归零", oppositePlayer().getName()));
            gameset(thisPlayer());
        }
    }

    public void measureFollows() {
        // msg("——————结算卡牌状态——————");
        // 立即结算受伤状态
        List<Damage> incommingDamagesCopy = new ArrayList<>(incommingDamages);
        incommingDamages = new ArrayList<>();
        incommingDamagesCopy.forEach(damage -> {
            damage.getTo().useEffects(EffectTiming.AfterDamaged, damage);
        });

        Map<Card, EventType> eventsCopy = events;
        events = new HashMap<>();
        // 再结算其他状态
        eventsCopy.forEach((card, type) -> {
            switch (type) {
                case Destroy -> {
                    if (card instanceof AreaCard areaCard)
                        areaCard.destroyed();
                }
            }
        });

        assert events.isEmpty();
    }

    public void gameset(PlayerInfo winner) {
        AiRegistry.unregister(this);
        gameset = true;

        // 计算对战持续时间
        long battleDuration = (System.currentTimeMillis() - battleStartTime) / 1000; // 转换为秒

        String victoryMsg = "游戏结束,获胜者：" + winner.getName();
        msg(victoryMsg);
        addBattleLog(victoryMsg);
        addBattleLog(String.format("对战持续：%d秒，共%d回合", battleDuration, totalTurns));

        pushInfo();
        final Session winnerSession = winner.getSession();
        final PlayerInfo loser = anotherPlayerBySession(winnerSession);
        final Session loserSession = loser != null ? loser.getSession() : null;

        // 保存对战记录
        try {
            saveBattleRecord(winner, loser, battleDuration);
        } catch (Exception e) {
            log.error("保存对战记录失败", e);
        }

        // 如果是弥留之国模式，发送重定向消息并进行结算
        boolean isBorderlandMode = getRoom() != null && getRoom().startsWith("borderland-");
        if (isBorderlandMode) {
            // 弥留之国对战结算
            try {
                Long winnerId = winnerSession != null ? sessionUserIds.get(winnerSession) : null;
                Long loserId = loserSession != null ? sessionUserIds.get(loserSession) : null;

                if (winnerId != null && loserId == null) {
                    // 玩家击败AI的情况
                    try {
                        org.example.user.service.BorderlandService borderlandService = org.example.system.util.SpringContext
                                .getBean(org.example.user.service.BorderlandService.class);

                        // 使用带卡组参数的方法
                        if (aiInitialDeckCodes != null && !aiInitialDeckCodes.isEmpty()) {
                            String rewardCard = borderlandService.winAgainstAI(winnerId, aiInitialDeckCodes);
                            if (winnerSession != null) {
                                if (rewardCard != null && !rewardCard.isEmpty()) {
                                    Msg.send(winnerSession, "alert", String.format(
                                            "🎉 胜利！\n\n击败AI获得：\n• 卡牌：%s\n• 签证延长1天", rewardCard));
                                } else {
                                    Msg.send(winnerSession, "alert", "🎉 胜利！\n\n击败AI获得：\n• 签证延长1天");
                                }
                            }
                            log.info("玩家 {} 在弥留之国击败了AI，获得卡牌 [{}]，签证延长1天", winnerId, rewardCard);
                        } else {
                            // 如果没有AI卡组信息，只延长天数
                            borderlandService.winAgainstAI(winnerId);
                            if (winnerSession != null) {
                                Msg.send(winnerSession, "alert", "🎉 胜利！\n\n签证延长1天");
                            }
                            log.info("玩家 {} 在弥留之国击败了AI，签证延长1天", winnerId);
                        }

                        // 保存战斗记录到数据库
                        org.example.user.service.BorderlandBattleLogService battleLogService = org.example.system.util.SpringContext
                                .getBean(org.example.user.service.BorderlandBattleLogService.class);

                        String winnerUserName = winnerSession != null ? userNames.get(winnerSession) : "未知玩家";

                        BorderlandBattleLog victoryLog = new BorderlandBattleLog(
                                "victory",
                                winnerUserName,
                                "弥留AI",
                                winnerUserName,
                                null);
                        battleLogService.save(victoryLog);
                        broadcastBattleLog(victoryLog);
                    } catch (Exception e) {
                        log.error("弥留之国AI对战结算失败", e);
                        if (winnerSession != null) {
                            Msg.send(winnerSession, "alert", "胜利！但结算出现错误");
                        }
                    }
                } else if (winnerId == null && loserId != null) {
                    // AI击败玩家的情况
                    try {
                        org.example.user.service.BorderlandService borderlandService = org.example.system.util.SpringContext
                                .getBean(org.example.user.service.BorderlandService.class);

                        borderlandService.loseAgainstAI(loserId);
                        if (loserSession != null) {
                            Msg.send(loserSession, "alert", "💀 失败！\n\n被AI击败：\n• 签证失效\n• 卡组清空\n• 进入1分钟惩罚期");
                        }

                        // 保存战斗记录到数据库 (1分钟 = 60秒)
                        org.example.user.service.BorderlandBattleLogService battleLogService = org.example.system.util.SpringContext
                                .getBean(org.example.user.service.BorderlandBattleLogService.class);

                        String loserUserName = loserSession != null ? userNames.get(loserSession) : "未知玩家";

                        BorderlandBattleLog defeatLog = new BorderlandBattleLog(
                                "defeat",
                                loserUserName,
                                "弥留AI",
                                "弥留AI",
                                60 // 1分钟惩罚
                        );
                        battleLogService.save(defeatLog);
                        broadcastBattleLog(defeatLog);

                        log.info("玩家 {} 在弥留之国输给了AI，签证失效，进入惩罚期", loserId);
                    } catch (Exception e) {
                        log.error("弥留之国AI对战失败结算错误", e);
                        if (loserSession != null) {
                            Msg.send(loserSession, "alert", "失败！结算出现错误");
                        }
                    }
                } else if (winnerId != null && loserId != null) {
                    // 玩家对玩家的情况（PVP入侵模式）
                    try {
                        org.example.user.service.BorderlandService borderlandService = org.example.system.util.SpringContext
                                .getBean(org.example.user.service.BorderlandService.class);

                        // 获取败者的卡组和天数信息
                        org.example.user.entity.BorderlandVisa loserVisa = borderlandService.getVisaStatus(loserId);
                        int loserCards = 0;
                        int loserDays = 0;
                        if (loserVisa != null) {
                            String deckData = loserVisa.getDeckData();
                            loserCards = (deckData != null && !deckData.isEmpty()) ? deckData.split(",").length : 0;
                            loserDays = loserVisa.getDaysRemaining();
                        }

                        borderlandService.settleBattle(winnerId, loserId, false);

                        if (winnerSession != null) {
                            Msg.send(winnerSession, "alert", String.format(
                                    "🎉 PVP胜利！\n\n夺取对手：\n• %d张卡牌\n• %d天签证",
                                    loserCards, loserDays));
                        }
                        if (loserSession != null) {
                            Msg.send(loserSession, "alert", String.format(
                                    "💀 PVP失败！\n\n失去全部：\n• %d张卡牌\n• %d天签证\n• 进入24小时惩罚期",
                                    loserCards, loserDays));
                        }

                        // 保存战斗记录到数据库 (24小时 = 86400秒)
                        org.example.user.service.BorderlandBattleLogService battleLogService = org.example.system.util.SpringContext
                                .getBean(org.example.user.service.BorderlandBattleLogService.class);

                        String winnerUserName = winnerSession != null ? userNames.get(winnerSession) : "未知玩家";
                        String loserUserName = loserSession != null ? userNames.get(loserSession) : "未知玩家";

                        BorderlandBattleLog pvpLog = new BorderlandBattleLog(
                                "victory",
                                winnerUserName,
                                loserUserName,
                                winnerUserName,
                                86400 // 败者24小时惩罚
                        );
                        battleLogService.save(pvpLog);
                        broadcastBattleLog(pvpLog);

                        log.info("弥留之国PVP结算: 胜者={}, 败者={}", winnerId, loserId);
                    } catch (Exception e) {
                        log.error("弥留之国PVP结算失败", e);
                        if (winnerSession != null) {
                            Msg.send(winnerSession, "alert", "PVP胜利！但结算出现错误");
                        }
                        if (loserSession != null) {
                            Msg.send(loserSession, "alert", "PVP失败！结算出现错误");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("弥留之国结算失败", e);
            }

            // 最后发送重定向
            if (winnerSession != null) {
                Msg.send(winnerSession, "redirect", "borderland.html");
            }
            if (loserSession != null) {
                Msg.send(loserSession, "redirect", "borderland.html");
            }
        } else {
            // 普通模式的简单消息
            if (winnerSession != null) {
                Msg.send(winnerSession, "alert", "你赢了！");
            }
            if (loserSession != null) {
                Msg.send(loserSession, "alert", "你输了！");
            }
        }

        // 释放资源
        roomGame.remove(getRoom());
        // 退出房间
        try {
            Session thisSession = thisPlayer().getSession();
            Session oppositeSession = oppositePlayer().getSession();

            if (thisSession != null) {
                userRoom.remove(thisSession);
                msgToThisPlayer("离开房间成功");
            }
            if (oppositeSession != null) {
                userRoom.remove(oppositeSession);
                msgToOppositePlayer("离开房间成功");
            }

            if (rope != null) {
                rope.cancel(true);
            }
            ScheduledExecutorService ses = roomSchedule.get(getRoom());
            if (ses != null) {
                ses.shutdown();
                roomSchedule.remove(getRoom());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Game Set");
    }

    public PlayerInfo thisPlayer() {
        return playerInfos[turnPlayer];
    }

    public PlayerInfo oppositePlayer() {
        return playerInfos[1 - turnPlayer];
    }

    public PlayerInfo playerBySession(Session session) {
        if (playerInfos[0].session == session) {
            return playerInfos[0];
        } else {
            return playerInfos[1];
        }
    }

    public PlayerInfo anotherPlayerBySession(Session session) {
        if (playerInfos[0].session == session) {
            return playerInfos[1];
        } else {
            return playerInfos[0];
        }
    }

    // region effect
    public boolean addEvent(Card card, EventType type) {
        EventType oldType = events.get(card);
        if (oldType != null) {
            // msg(card.getNameWithOwner() + "已经被" + oldType.getName() + "，无法再被" +
            // type.getName());
            return false;
        }
        // msg(card.getNameWithOwner() + "的" + type.getName() + "状态已加入队列");
        events.put(card, type);
        return true;
    }

    public void useAreaCardEffectBatch(List<AreaCard> cards, EffectTiming timing) {
        List<GameObj> gameObjs = cards.stream().map(p -> (GameObj) p).toList();
        tempEffectBatch(gameObjs, timing);
        startEffect();
    }

    public void useAreaCardEffectBatch(List<AreaCard> cards, EffectTiming timing, Object param) {
        List<GameObj> gameObjs = cards.stream().map(p -> (GameObj) p).toList();
        tempEffectBatch(gameObjs, timing, param);
        startEffect();
    }

    public void useEffectBatch(List<GameObj> objs, EffectTiming timing) {
        tempEffectBatch(objs, timing);
        startEffect();
    }

    public void tempEffectBatch(List<GameObj> objs, EffectTiming timing, Object param) {
        objs.forEach(obj -> obj.tempEffects(timing, param));
    }

    public void tempEffectBatch(List<GameObj> objs, EffectTiming timing) {
        objs.forEach(obj -> obj.tempEffects(timing));
    }

    public void tempCardEffectBatch(List<Card> objs, EffectTiming timing) {
        objs.forEach(obj -> obj.tempEffects(timing));
    }

    public void tempCardEffectBatch(List<Card> objs, EffectTiming timing, Object param) {
        objs.forEach(obj -> obj.tempEffects(timing, param));
    }

    public void tempAreaCardEffectBatch(List<AreaCard> objs, EffectTiming timing) {
        objs.forEach(obj -> obj.tempEffects(timing));
    }

    public void tempAreaCardEffectBatch(List<AreaCard> objs, EffectTiming timing, Object param) {
        objs.forEach(obj -> obj.tempEffects(timing, param));
    }

    public void tempEffect(Effect.EffectInstance instance) {
        Effect effect = instance.getEffect();
        // 入队（尾部加入），保证 FIFO
        effectInstances.offer(instance);
        // msg(effect.getOwnerObj().getNameWithOwner()+"的【"+effect.getTiming().getName()+"】效果已加入队列"
        // +
        // "（队列现在有" + effectInstances.size() + "个效果）");
    }

    // 结算效果
    public void startEffect() {

        if (inSettle)
            return;
        inSettle = true;
        // msg("——————开始结算——————");

        consumeEffectChain(chainDeep);
        // 计算主战者死亡状况
        measureLeader();
        inSettle = false;
    }

    public void consumeEffectChain(int deep) {
        // msg("——————开始触发事件——————");
        measureFollows();
        // msg("——————开始触发效果——————");
        consumeEffect();
        // msg("——————停止触发效果——————");

        if (hasEvent()) {
            if (!canChain || deep == 0) {
                msg("停止连锁！本次死亡结算后不触发任何效果");
                measureFollows();
                effectInstances.clear();
                events.clear();
                return;
            }
            // msg("——————事件连锁（"+deep+"）——————");
            consumeEffectChain(deep - 1);
        }
    }

    public void consumeEffect() {
        // 按队列（FIFO）依次处理效果实例，处理过程中若有新效果加入队列则继续处理
        while (!effectInstances.isEmpty()) {
            Effect.EffectInstance instance = effectInstances.poll();
            try {
                instance.consume();
            } catch (RuntimeException e) {
                // 如果是游戏结束异常，需要重新抛出
                if ("Game Set".equals(e.getMessage())) {
                    throw e;
                }
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    // endregion effect

    // region event

    public void transform(Card fromCard, Card toCard) {
        msg(fromCard.getNameWithOwnerWithPlace() + "变身成了" + toCard.getId());
        if (fromCard.atArea()) {
            if (fromCard.hasKeyword("魔法免疫")) {
                fromCard.getInfo().msg(fromCard.getNameWithOwner() + "免疫了本次变身！");
                return;
            }
            if (fromCard.hasKeyword("魔法护盾")) {
                fromCard.getInfo().msg(fromCard.getNameWithOwner() + "的魔法护盾抵消了本次变身！");
                fromCard.removeKeyword("魔法护盾");
                return;
            }
            List<AreaCard> area = fromCard.ownerPlayer().getArea();
            int index = area.indexOf(fromCard);
            area.remove(index);
            fromCard.useEffects(EffectTiming.WhenNoLongerAtArea);
            // 要变成随从
            if (toCard instanceof AreaCard areaCard) {
                area.add(index, areaCard);
                areaCard.useEffects(EffectTiming.WhenAtArea);
            } else {
                msg(toCard.getNameWithOwner() + "无法留在战场而被除外！");
                exile(toCard);
            }

        } else {
            List<Card> where = fromCard.where();
            int index = where.indexOf(fromCard);
            where.remove(index);
            where.add(index, toCard);
        }
    }

    public void exile(Card card) {
        exile(List.of(card));
    }

    public void exile(List<Card> cards) {
        if (cards.isEmpty())
            return;
        msg(cards.stream().map(Card::getNameWithOwner).collect(Collectors.joining("、")) + "从游戏中除外！");
        cards.forEach(card -> {
            if (card.where() == null)
                return;

            // 场上卡除外时，有机会发动离场时效果
            if (card.atArea() && card instanceof AreaCard areaCard) {
                if (areaCard.hasKeyword("魔法免疫")) {
                    areaCard.getInfo().msg(areaCard.getNameWithOwner() + "免疫了本次除外！");
                    return;
                }
                if (areaCard.hasKeyword("魔法护盾")) {
                    areaCard.getInfo().msg(areaCard.getNameWithOwner() + "的魔法护盾抵消了本次除外！");
                    areaCard.removeKeyword("魔法护盾");
                    return;
                }
                card.removeWhenAtArea();
                card.tempEffects(EffectTiming.Leaving);
                // 场上随从除外时，装备也除外
                if (card instanceof FollowCard followCard && followCard.equipped())
                    exile(followCard.getEquipment());
            } else
                card.removeWhenNotAtArea();

            card.tempEffects(EffectTiming.Exile);
            if (card.hasKeyword("恶魔转生")) {
                List<Card> totalCard = new ArrayList<>();
                totalCard.addAll(thisPlayer().getHand().stream()
                        .filter(c -> c instanceof FollowCard f
                                && !f.hasKeyword("恶魔转生"))
                        .toList());
                totalCard.addAll(thisPlayer().getArea().stream()
                        .filter(c -> c instanceof FollowCard f
                                && !f.hasKeyword("恶魔转生"))
                        .toList());
                totalCard.addAll(thisPlayer().getGraveyard().stream()
                        .filter(c -> c instanceof FollowCard f
                                && !f.hasKeyword("恶魔转生"))
                        .toList());
                totalCard.addAll(thisPlayer().getDeck().stream()
                        .filter(c -> c instanceof FollowCard f
                                && !f.hasKeyword("恶魔转生"))
                        .toList());
                totalCard.addAll(oppositePlayer().getHand().stream()
                        .filter(c -> c instanceof FollowCard f
                                && !f.hasKeyword("恶魔转生"))
                        .toList());
                totalCard.addAll(oppositePlayer().getArea().stream()
                        .filter(c -> c instanceof FollowCard f
                                && !f.hasKeyword("恶魔转生"))
                        .toList());
                totalCard.addAll(oppositePlayer().getGraveyard().stream()
                        .filter(c -> c instanceof FollowCard f
                                && !f.hasKeyword("恶魔转生"))
                        .toList());
                totalCard.addAll(oppositePlayer().getDeck().stream()
                        .filter(c -> c instanceof FollowCard f
                                && !f.hasKeyword("恶魔转生"))
                        .toList());

                if (totalCard.isEmpty()) {
                    msg("游戏中只剩下恶魔牌，" + card.getNameWithOwner() + "已经无法转生");
                    return;
                }
                Card luckyCard = Lists.randOf(totalCard);

                Card newCard = card.createCard(card.getClass());
                transform(luckyCard, newCard);
            }
        });
        startEffect();
    }

    public void damageMulti(GameObj from, List<GameObj> objs, int damage) {
        List<Damage> damages = objs.stream().map(obj -> new Damage(from, obj, damage)).toList();
        new DamageMulti(this, damages).apply();
    }

    public void damageAttacking(FollowCard from, GameObj to) {
        if (to instanceof FollowCard && !from.hasKeyword("远程") && !((FollowCard) to).hasKeyword("眩晕"))
            new DamageMulti(this, List.of(new Damage(from, to), new Damage(to, from))).apply();
        else
            new DamageMulti(this, List.of(new Damage(from, to))).apply();
    }

    public void damageEffect(GameObj from, GameObj to, int damage) {
        new DamageMulti(this, List.of(new Damage(from, to, damage))).apply();
    }

    // endregion event

    public List<AreaCard> getAreaCardsCopy() {
        List<AreaCard> _result = new ArrayList<>();
        _result.addAll(thisPlayer().getArea());
        _result.addAll(oppositePlayer().getArea());
        return _result;
    }

    public List<GameObj> getTargetableGameObj() {
        List<GameObj> _result = new ArrayList<>();
        _result.addAll(thisPlayer().getAreaFollows());
        _result.addAll(oppositePlayer().getAreaFollows());
        _result.add(thisPlayer().getLeader());
        _result.add(oppositePlayer().getLeader());
        return _result;
    }

    public List<AreaCard> getAreaFollowsCopy() {
        List<AreaCard> _result = new ArrayList<>();
        _result.addAll(thisPlayer().getAreaFollows());
        _result.addAll(oppositePlayer().getAreaFollows());
        return _result;
    }

    public List<GameObj> getAreaFollowsAsGameObj() {
        List<GameObj> _result = new ArrayList<>();
        _result.addAll(thisPlayer().getAreaFollows());
        _result.addAll(oppositePlayer().getAreaFollows());
        return _result;
    }

    // region turn
    public void beginGame() {
        Leader leader = thisPlayer().getLeader();
        leader.setCanUseSkill(true);
        leader.useEffects(EffectTiming.BeginGame);

        Leader enemyLeader = oppositePlayer().getLeader();
        enemyLeader.useEffects(EffectTiming.BeginGame);

        Msg.send(thisPlayer().getSession(), "swapOver", "");
        Msg.send(oppositePlayer().getSession(), "swapOver", "");

    }

    public void zeroTurn(Session u0, Session u1) {
        zeroTurnWithDecks(u0, userDecks.get(u0), userNames.get(u0),
                u1, userDecks.get(u1), userNames.get(u1));
    }

    public void zeroTurnWithDecks(Session u0, PlayerDeck deck0, String name0,
            Session u1, PlayerDeck deck1, String name1) {

        Objects.requireNonNull(deck0, "Deck for first player is missing");
        Objects.requireNonNull(deck1, "Deck for second player is missing");

        // 初始化对战统计
        battleStartTime = System.currentTimeMillis();
        totalTurns = 0;
        battleLog.clear();
        if (aiInitialDeckCodes != null && !aiInitialDeckCodes.isEmpty()) {
            battleMode = "borderland";
            addBattleLog("对战模式：弥留之国 - AI对战");
        } else {
            battleMode = "normal";
            addBattleLog("对战模式：常规匹配");
        }

        PlayerInfo p0 = thisPlayer();
        p0.setSession(u0);
        p0.setName(name0 != null ? name0 : "Player A");
        // 为玩家1随机分配英雄技能
        Class<? extends Leader> skill0 = LeaderSkillFactory.getRandomSkill();
        p0.setLeader(createLeader(skill0, 0));
        p0.setDeck(deck0.getActiveDeckInstance(0, this));
        Collections.shuffle(p0.getDeck());

        PlayerInfo p1 = oppositePlayer();
        p1.setSession(u1);
        p1.setName(name1 != null ? name1 : "Player B");
        // 为玩家2随机分配英雄技能
        Class<? extends Leader> skill1 = LeaderSkillFactory.getRandomSkill();
        p1.setLeader(createLeader(skill1, 1));
        p1.setDeck(deck1.getActiveDeckInstance(1, this));
        Collections.shuffle(p1.getDeck());

        p0.getLeader().init();
        p1.getLeader().init();

        addBattleLog(String.format("%s 使用 %s", p0.getName(), p0.getLeader().getName()));
        addBattleLog(String.format("%s 使用 %s", p1.getName(), p1.getLeader().getName()));

        p0.draw(3);
        p1.draw(3);
        msg("游戏开始，请选择3张手牌交换");
        Msg.send(p0.getSession(), "swap", p0.getHand());
        Msg.send(p1.getSession(), "swap", p1.getHand());
    }

    /**
     * 创建 Leader 实例
     */
    private Leader createLeader(Class<? extends Leader> leaderClass, int owner) {
        try {
            Leader leader = leaderClass.getDeclaredConstructor().newInstance();
            leader.setOwner(owner);
            leader.setInfo(this);
            return leader;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create leader: " + leaderClass.getName(), e);
        }
    }

    public void startTurn() {
        totalTurns++; // 增加回合计数
        thisPlayer().clearCount(PLAY_NUM);
        thisPlayer().getPlayedCard().clear();
        if (thisPlayer().ppMax < thisPlayer().getPpLimit()) {
            thisPlayer().ppMax++;
        }
        thisPlayer().ppNum = thisPlayer().ppMax;
        String turnMsg = "第" + turn + "回合：" + thisPlayer().getName() + "的回合，有" + thisPlayer().ppNum + "pp";
        msg(turnMsg);
        addBattleLog(turnMsg);
        beforeTurn();
        thisPlayer().draw(1);

        ScheduledExecutorService scheduler = roomSchedule.get(getRoom());
        if (scheduler == null) {
            log.error("roomSchedule为null! room={}, roomSchedule keys={}", getRoom(), roomSchedule.keySet());
            // 尝试重新初始化
            scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);
            roomSchedule.put(getRoom(), scheduler);
            log.info("已重新初始化roomSchedule for room={}", getRoom());
        }

        // 从配置中获取超时时间
        GameConfig config = SpringContext.getBean(GameConfig.class);
        int timeoutSeconds = thisPlayer().isShortRope() ? config.getShortRopeSeconds() : config.getTurnTimeoutSeconds();

        rope = scheduler.schedule(this::endTurnOfTimeout, timeoutSeconds, TimeUnit.SECONDS);
        msg("倒计时" + timeoutSeconds + "秒！");
        pushInfo();
        msgToThisPlayer("请出牌！");
        msgToOppositePlayer("等待对手出牌......");
        Msg.send(thisPlayer().getSession(), "yourTurn", "");
        Msg.send(oppositePlayer().getSession(), "enemyTurn", "");

        if (turn == 10) {// TODO 活动模式，第10回合奖励
            final List<Class<? extends Card>> classes = CardPackage.randCard("passive", 3);
            final List<Card> list = classes.stream().map(clazz -> (Card) thisPlayer().getLeader().createCard(clazz))
                    .toList();
            thisPlayer().discoverCard(list, card -> card.getPlay().effect().accept(0, new ArrayList<>()));
        }

        AiRegistry.onTurnStart(this);
    }

    public void endTurnOfTimeout() {
        thisPlayer().setShortRope(true);
        endTurn();
    }

    public void endTurnOfCommand() {
        thisPlayer().setShortRope(false);
        rope.cancel(true);
        endTurn();
    }

    public void endTurn() {
        thisPlayer().autoDiscover();
        msg(thisPlayer().getName() + "的回合结束");
        try {
            afterTurn();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (thisPlayer().getStep() == -1)
            return;// 回合结束效果触发了重启游戏
        // 是否有追加回合
        if (moreTurn > 0) {
            moreTurn--;
        } else {
            turn += turnPlayer;// 如果是玩家1就加回合数
            turnPlayer = 1 ^ turnPlayer;
        }
        msg("——————————");

        startTurn();
    }

    public void beforeTurn() {

        // 场上随从驻场回合+1、攻击次数清零
        // 发动回合开始效果
        // 场上护符倒数-1
        oppositePlayer().getAreaCopy().forEach(enemyAreaCard -> {
            if (!enemyAreaCard.atArea())
                return;

            enemyAreaCard.useEffects(EffectTiming.EnemyBeginTurn);
            if (!enemyAreaCard.atArea())
                return;

            if (enemyAreaCard instanceof FollowCard followCard && followCard.equipped()) {
                EquipmentCard equipment = followCard.getEquipment();
                equipment.useEffects(EffectTiming.EnemyBeginTurn);
            }

            if (enemyAreaCard instanceof FollowCard followCard) {
                followCard.setTurnAttack(0);
                followCard.removeKeyword("眩晕");
                followCard.removeKeyword("冻结");
                // followCard.removeKeywordAll("格挡");
            }
        });
        thisPlayer().getAreaCopy().forEach(areaCard -> {
            if (!areaCard.atArea())
                return;

            areaCard.useEffects(EffectTiming.BeginTurn);
            if (!areaCard.atArea())
                return;

            if (areaCard instanceof FollowCard followCard && followCard.equipped()) {
                EquipmentCard equipment = followCard.getEquipment();
                equipment.useEffects(EffectTiming.BeginTurn);
            }
            if (!areaCard.atArea())
                return;

            if (areaCard instanceof FollowCard followCard) {
                int turnAgePlus = followCard.getTurnAge() + 1;
                followCard.setTurnAge(turnAgePlus);
                followCard.setTurnAttack(0);
            }

            if (areaCard instanceof AmuletCard amuletCard) {
                int countDown = amuletCard.getCountDown();
                if (countDown > 0) {
                    amuletCard.countDown();
                }
            }
        });

        // 查找牌堆是否有瞬召卡牌，同名字卡牌各取一张
        Map<String, GameObj> nameCard = thisPlayer().getDeck().stream()
                .collect(Collectors.toMap(Card::getName, o -> o, (a, b) -> a));

        // 瞬召卡牌
        useEffectBatch(new ArrayList<>(nameCard.values()), EffectTiming.InvocationBegin);

        // 主战者技能重置、发动主战者效果和手牌效果
        Leader leader = thisPlayer().getLeader();
        leader.setCanUseSkill(true);
        leader.useEffects(EffectTiming.BeginTurn);
        thisPlayer().getHandCopy().forEach(card -> card.useEffects(EffectTiming.BeginTurnAtHand));

        Leader enemyLeader = oppositePlayer().getLeader();
        enemyLeader.useEffects(EffectTiming.EnemyBeginTurn);
        oppositePlayer().getHandCopy().forEach(card -> card.useEffects(EffectTiming.EnemyBeginTurnAtHand));
    }

    public void afterTurn() {
        // 对手中毒效果
        final Integer poison = oppositePlayer().getCount(POISON);
        if (poison > 0) {
            msg(oppositePlayer().getLeader().getNameWithOwner() + "受到" + poison + "点中毒伤害");
            damageEffect(thisPlayer().getLeader(), oppositePlayer().getLeader(), poison);
            oppositePlayer().count(POISON, -1);
        }

        oppositePlayer().getAreaCopy().forEach(areaCard -> {
            if (areaCard instanceof FollowCard followCard && followCard.hasKeyword("中毒")) {
                final int poison1 = followCard.countKeyword("中毒");
                msg(followCard.getNameWithOwner() + "受到" + poison1 + "点中毒伤害");
                damageEffect(followCard, followCard, poison1);
            }
        });

        // 发动回合结束效果
        oppositePlayer().getAreaCopy().forEach(areaCard -> {
            if (!areaCard.atArea())
                return;

            areaCard.useEffects(EffectTiming.EnemyEndTurn);
            if (!areaCard.atArea())
                return;

            if (areaCard instanceof FollowCard followCard && followCard.equipped()) {
                EquipmentCard equipment = followCard.getEquipment();
                equipment.useEffects(EffectTiming.EnemyEndTurn);
            }
        });
        thisPlayer().getAreaCopy().forEach(areaCard -> {
            if (!areaCard.atArea())
                return;

            areaCard.useEffects(EffectTiming.EndTurn);
            if (!areaCard.atArea())
                return;

            if (areaCard instanceof FollowCard followCard && followCard.equipped()) {
                EquipmentCard equipment = followCard.getEquipment();
                equipment.useEffects(EffectTiming.EndTurn);
            }
        });
        thisPlayer().getHandCopy().forEach(card -> {
            if (card.hasKeyword("虚无")) {
                thisPlayer().abandon(card);
            }
        });

        // 查找牌堆是否有瞬召卡牌，同名字卡牌各取一张
        Map<String, GameObj> nameCard = thisPlayer().getDeck().stream()
                .collect(Collectors.toMap(Card::getName, o -> o, (a, b) -> a));

        // 瞬召卡牌
        useEffectBatch(new ArrayList<>(nameCard.values()), EffectTiming.InvocationEnd);

        // 发动主战者效果
        Leader leader = thisPlayer().getLeader();
        leader.useEffects(EffectTiming.EndTurn);
        leader.expireEffect();
        thisPlayer().getHandCopy().forEach(card -> card.useEffects(EffectTiming.EndTurnAtHand));
        thisPlayer().setHandPlayable(card -> true);

        Leader enemyLeader = oppositePlayer().getLeader();
        enemyLeader.useEffects(EffectTiming.EnemyEndTurn);
        enemyLeader.expireEffect();
        oppositePlayer().getHandCopy().forEach(card -> card.useEffects(EffectTiming.EnemyEndTurnAtHand));
    }

    public void addMoreTurn() {
        moreTurn++;
    }

    /**
     * 保存对战记录到数据库
     */
    private void saveBattleRecord(PlayerInfo winner, PlayerInfo loser, long duration) {
        try {
            org.example.user.mapper.BattleRecordMapper battleRecordMapper = org.example.system.util.SpringContext
                    .getBean(org.example.user.mapper.BattleRecordMapper.class);

            org.example.user.entity.BattleRecord record = new org.example.user.entity.BattleRecord();

            // 设置胜负双方ID
            Session winnerSession = winner.getSession();
            Session loserSession = loser != null ? loser.getSession() : null;

            Long winnerId = winnerSession != null ? sessionUserIds.get(winnerSession) : null;
            Long loserId = loserSession != null ? sessionUserIds.get(loserSession) : null;

            record.setWinnerId(winnerId);
            record.setLoserId(loserId);
            record.setMode(battleMode != null ? battleMode : "normal");

            // 设置卡组信息
            List<String> winnerDeckCodes = winner.getDeck().stream()
                    .map(Card::getClass)
                    .map(Class::getSimpleName)
                    .collect(Collectors.toList());
            record.setWinnerDeck(String.join(",", winnerDeckCodes));
            record.setWinnerLeader(winner.getLeader().getName());

            if (loser != null) {
                List<String> loserDeckCodes = loser.getDeck().stream()
                        .map(Card::getClass)
                        .map(Class::getSimpleName)
                        .collect(Collectors.toList());
                record.setLoserDeck(String.join(",", loserDeckCodes));
                record.setLoserLeader(loser.getLeader().getName());
            }

            // 设置对战统计
            record.setDuration((int) duration);
            record.setTotalTurns(totalTurns);
            record.setEndReason(endReason != null ? endReason : "hp_zero");

            // 设置对战详情（保存所有日志）
            String detailsLog = String.join("\n", battleLog);
            record.setBattleDetails(detailsLog);

            record.setCreatedAt(java.time.OffsetDateTime.now());

            battleRecordMapper.insert(record);

            log.info("对战记录已保存: 胜者={}, 败者={}, 模式={}, 时长={}秒, 回合数={}",
                    winnerId, loserId, record.getMode(), duration, totalTurns);
        } catch (Exception e) {
            log.error("保存对战记录异常", e);
        }
    }

    /**
     * 广播战斗记录给所有在线玩家
     */
    private void broadcastBattleLog(BorderlandBattleLog battleLog) {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("type", battleLog.getEventType());
        data.put("player1", battleLog.getPlayer1Name());
        data.put("player2", battleLog.getPlayer2Name());
        data.put("winner", battleLog.getWinnerName());
        data.put("timestamp", battleLog.getTimestamp().toString());
        data.put("punishmentSeconds", battleLog.getPunishmentSeconds());

        // 广播给所有玩家
        for (Session session : userNames.keySet()) {
            try {
                Msg.send(session, "borderland-battle-log", data);
            } catch (Exception e) {
                log.warn("Failed to broadcast battle log to session", e);
            }
        }
    }
    // endregion turn
}
