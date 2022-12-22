package org.example.game;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.Data;
import org.example.card.*;
import org.example.constant.EffectTiming;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.system.Database.*;
import static org.example.system.Database.userDecks;

@Data
public class GameInfo {
    SocketIOServer server;
    String room;

    int turn;
    int turnPlayer;
    boolean gameset;
    ScheduledFuture<?> rope;

    PlayerInfo[] playerInfos;

    public GameInfo(SocketIOServer server, String room) {
        this.room = room;
        this.server = server;
        this.turn = 1;
        this.turnPlayer = 0;
        this.gameset = false;
        this.playerInfos = new PlayerInfo[2];
        this.playerInfos[0] = new PlayerInfo(this);
        this.playerInfos[1] = new PlayerInfo(this);

        msg("比赛开始，请选择三张手牌交换");

    }

    public void msg(String msg){
        server.getRoomOperations(room).sendEvent("receiveMsg", msg);
    }

    public void msgTo(UUID uuid, String msg){
        server.getClient(uuid).sendEvent("receiveMsg", msg);
    }

    public void msgToThisPlayer(String msg){
        server.getClient(thisPlayer().getUuid()).sendEvent("receiveMsg", msg);
    }
    public void msgToOppositePlayer(String msg){
        server.getClient(oppositePlayer().getUuid()).sendEvent("receiveMsg", msg);
    }

    public void gameset(PlayerInfo winner){
        gameset = true;
        msg("游戏结束，获胜者："+winner.getName());

        // 释放资源
        roomReadyMatch.remove(getRoom());
        roomGame.remove(getRoom());
        roomSchedule.get(getRoom()).shutdown();
        roomSchedule.remove(getRoom());
        // 退出房间
        server.getClient(thisPlayer().getUuid()).leaveRoom(getRoom());
        server.getClient(oppositePlayer().getUuid()).leaveRoom(getRoom());
    }

    public PlayerInfo thisPlayer(){
        return playerInfos[turnPlayer];
    }
    public PlayerInfo oppositePlayer(){
        return playerInfos[1-turnPlayer];
    }
    public PlayerInfo playerByUuid(UUID uuid){
        if(playerInfos[0].uuid == uuid){
            return playerInfos[0];
        }else {
            return playerInfos[1];
        }
    }
    public PlayerInfo anotherPlayerByUuid(UUID uuid){
        if(playerInfos[0].uuid == uuid){
            return playerInfos[1];
        }else {
            return playerInfos[0];
        }
    }

    public void destroy(List<AreaCard> cards){
        List<AreaCard> cardsCopy = new ArrayList<>(cards);
        cardsCopy.forEach(AreaCard::death);
    }

    public void damageLeader(Leader leader,int damage){
        if(thisPlayer().getLeader().equals(leader)){
            int hp = thisPlayer().getHp();
            thisPlayer().setHp(hp - damage);
            msg(thisPlayer().getName()+"的主战者受到"+damage+"点伤害！（剩余"+thisPlayer().getHp()+"点生命值）");
            if(hp < 0){
                gameset(oppositePlayer());
            }
        }else {
            int hp = oppositePlayer().getHp();
            oppositePlayer().setHp(hp - damage);
            msg(oppositePlayer().getName()+"的主战者受到"+damage+"点伤害！（剩余"+oppositePlayer().getHp()+"点生命值）");
            if (hp < 0) {
                gameset(thisPlayer());
            }
        }
    }

    public void zeroTurn(UUID u1, UUID u2){

        PlayerInfo p0 = thisPlayer();
        p0.setDeck(userDecks.get(u1).getActiveDeck());
        p0.setUuid(u1);
        p0.setName(userNames.get(u1));
        p0.shuffle();
        p0.draw(3);
        p0.count("allCost",0);
        msgToThisPlayer("你的手牌:\n"+p0.describeHand());

        PlayerInfo p1 = oppositePlayer();
        p1.setDeck(userDecks.get(u2).getActiveDeck());
        p1.setUuid(u2);
        p1.setName(userNames.get(u2));
        p1.shuffle();
        p1.draw(3);
        p1.count("allCost",0);
        msgToOppositePlayer("你的手牌:\n"+p1.describeHand());

    }

    public void startTurn(){
        beforeTurn();
        try {
            if(thisPlayer().ppMax<10){
                thisPlayer().ppMax++;
            }
            thisPlayer().ppNum = thisPlayer().ppMax;
            thisPlayer().draw(1);
            msg("第" + turn + "回合：" + thisPlayer().getName()+"的回合，有" + thisPlayer().ppNum + "pp");

            if(thisPlayer().isShortRope()){
                rope = roomSchedule.get(getRoom()).schedule(this::endTurnOfTimeout, 10, TimeUnit.SECONDS);
                msg("倒计时10秒！");
            }else{
                rope = roomSchedule.get(getRoom()).schedule(this::endTurnOfTimeout, 600, TimeUnit.SECONDS);
                msg("倒计时60秒！");
            }
            msgToThisPlayer(describeGame());
            msgToThisPlayer("请出牌！");
            msgToOppositePlayer("等待对手出牌......");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void endTurnOfTimeout(){
        thisPlayer().setShortRope(true);
        endTurn();
    }
    public void endTurnOfCommand(){
        thisPlayer().setShortRope(false);
        rope.cancel(true);
        endTurn();
    }

    public void endTurn(){
        msg(thisPlayer().getName()+"的回合结束");
        afterTurn();
        turn += turnPlayer;// 如果是玩家1就加回合数
        turnPlayer = 1 ^ turnPlayer;
        startTurn();
    }


    public void beforeTurn(){
        // 发动主战者效果
        List<Leader.Effect> usedUpEffects = new ArrayList<>();
        thisPlayer().getLeader().getEffects().stream()
            .filter(effect -> EffectTiming.BeginTurn.equals(effect.getTiming()))
            .forEach(effect -> {
                effect.getEffect().accept(thisPlayer());
                int canUse = effect.getCanUse();
                if(canUse == 1){
                    // 用完了销毁
                    usedUpEffects.add(effect);
                    msg(effect.getSource().getNameWithOwner() + "提供的主战者效果已用完");
                }else if (canUse > 1){
                    effect.setCanUse(canUse-1);
                }
            });
        thisPlayer().getLeader().getEffects().removeAll(usedUpEffects);

        // 场上随从驻场回合+1、攻击次数清零
        // 发动回合开始效果
        // 场上护符倒数-1
        thisPlayer().getArea().forEach(areaCard -> {
            if(areaCard instanceof FollowCard followCard){
                int turnAgePlus = followCard.getTurnAge() + 1;
                if(turnAgePlus>0){// 可能有随从会需要准备多个回合，还是判断下
                    msg(followCard.getNameWithOwner() + "可以攻击了！");
                }
                followCard.setTurnAge(turnAgePlus);

                followCard.setTurnAttack(0);
            }
            areaCard.effectBegin();
            if(areaCard instanceof AmuletCard amuletCard){
                int timer = amuletCard.getTimer();
                if(timer > 0){
                    amuletCard.setTimer(timer - 1);
                    msg(amuletCard.getNameWithOwner() + "的倒数-1");
                    if(amuletCard.getTimer() == 0){
                        amuletCard.death();
                    }
                }
            }
        });

        // 查找牌堆是否有瞬召卡片，同名字卡牌各取一张
        Map<String, Card> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o, (a,b)->a));

        List<Card> canInvocation =
            new ArrayList<>(nameCard.values().stream().filter(Card::canInvocationBegin).toList());

        // 法术卡揭示到手牌
        while(thisPlayer().getHand().size() < thisPlayer().getHandMax()){
            Optional<Card> first = canInvocation.stream().filter(card -> card instanceof SpellCard).findFirst();
            if(first.isEmpty()) break;

            // region 从牌堆召唤到手牌
            SpellCard card = (SpellCard)first.get();
            msg(thisPlayer().getName()+"揭示了"+card.getName());
            thisPlayer().getHand().add(card);
            thisPlayer().getDeck().remove(card);
            canInvocation.remove(card);
            card.afterInvocationBegin();
            // endregion

        }

        // 瞬念召唤到场上
        while(thisPlayer().getArea().size() < thisPlayer().getAreaMax()){
            Optional<Card> first = canInvocation.stream().filter(card -> card instanceof AreaCard).findFirst();
            if(first.isEmpty()) break;

            // region 从牌堆召唤到场上
            AreaCard card = (AreaCard)first.get();
            msg(thisPlayer().getName()+"瞬念召唤了"+card.getName());
            thisPlayer().summon(card);
            thisPlayer().getDeck().remove(card);
            canInvocation.remove(card);
            card.afterInvocationBegin();
            // endregion
        }

    }
    public void afterTurn(){
        // 发动主战者效果
        List<Leader.Effect> usedUpEffects = new ArrayList<>();
        thisPlayer().getLeader().getEffects().stream()
            .filter(effect -> EffectTiming.EndTurn.equals(effect.getTiming()))
            .forEach(effect -> {
                effect.getEffect().accept(thisPlayer());
                int canUse = effect.getCanUse();
                if(canUse == 1){
                    // 用完了销毁
                    usedUpEffects.add(effect);
                    msg(effect.getSource().getNameWithOwner() + "提供的主战者效果已用完");
                }else if (canUse > 1){
                    effect.setCanUse(canUse-1);
                }
            });
        thisPlayer().getLeader().getEffects().removeAll(usedUpEffects);

        // 发动回合结束效果
        List<AreaCard> areaCopy = new ArrayList<>(thisPlayer().getArea());
        areaCopy.forEach(AreaCard::effectEnd);

        // 查找牌堆是否有瞬召卡片
        Map<String, Card> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o, (a,b)->a));
        while(thisPlayer().getArea().size()<5){
            Optional<Card> first = nameCard.values().stream()
                .filter(card -> card instanceof AreaCard  areaCard && areaCard.canInvocationEnd()).findFirst();
            if (first.isEmpty()) {
                break;
            }
            // region 从牌堆召唤到场上
            AreaCard card = (AreaCard)first.get();
            msg(thisPlayer().getName()+"瞬念召唤了"+card.getName());
            thisPlayer().summon(card);
            thisPlayer().getDeck().remove(card);
            card.afterInvocationEnd();// 发动瞬念效果
            // endregion
        }
    }

    public String describeGame(){
        StringBuilder sb = new StringBuilder();
        PlayerInfo thisPlayer = thisPlayer();
        PlayerInfo oppositePlayer = oppositePlayer();

        sb.append("局面信息\n");
        sb.append("剩余pp：").append(thisPlayer.getPpNum()).append("\n");
        sb.append("\n");
        sb.append(oppositePlayer.name)
            .append("\t血：").append(oppositePlayer.getHp()).append("/").append(oppositePlayer.getHpMax())
            .append("\t牌：").append(oppositePlayer.deck.size())
            .append("\t墓：").append(oppositePlayer.graveyardCount)
            .append("\t手：").append(oppositePlayer.hand.size())
            .append("\n");
        sb.append(thisPlayer.name)
            .append("\t血：").append(thisPlayer.getHp()).append("/").append(thisPlayer.getHpMax())
            .append("\t牌：").append(thisPlayer.deck.size())
            .append("\t墓：").append(thisPlayer.graveyardCount)
            .append("\t手：").append(thisPlayer.hand.size())
            .append("\n");

        sb.append("\n");

        sb.append("敌方战场：\n");
        for (int i = 0; i < oppositePlayer.area.size(); i++) {
            Card card = oppositePlayer.area.get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getName()).append("\t");
             if("随从".equals(card.getType())){
                FollowCard follow = (FollowCard) card;
                sb.append(follow.getAtk()).append("攻\t");
                sb.append(follow.getHp()).append("/").append(follow.getMaxHp()).append("血\t");
            }
            sb.append("\n");
        }
        sb.append("我方战场：\n");
        for (int i = 0; i < thisPlayer.area.size(); i++) {
            Card card = thisPlayer.area.get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getName()).append("\t");
            if("随从".equals(card.getType())){
                FollowCard follow = (FollowCard) card;
                if(follow.getTurnAttack() < follow.getTurnAttackMax()){
                    if(follow.getTurnAge()>0){
                        sb.append("未攻击").append("\t");
                    }else if(follow.isDash()){
                        sb.append("突进").append("\t");
                    }
                }
                sb.append(follow.getAtk()).append("攻\t");
                sb.append(follow.getHp()).append("/").append(follow.getMaxHp()).append("血\t");
            }
            sb.append("\n");
        }
        sb.append("\n");

        sb.append("我的手牌：\n").append(thisPlayer.describeHand());

        return sb.toString();
    }
}
