package org.example.game;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.Getter;
import lombok.Setter;
import org.example.card.*;
import org.example.constant.EffectTiming;
import org.example.system.Lists;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.PLAY_NUM;
import static org.example.system.Database.*;

@Getter
@Setter
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
        this.playerInfos[0] = new PlayerInfo(this,true);
        this.playerInfos[1] = new PlayerInfo(this,false);

        msg("游戏开始，请选择3张手牌交换");

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

        throw new RuntimeException("Game Set");
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

    // TODO 变身效果
    public void transform(Card card, Class<? extends Card> cardClass){
    }
    public void destroy(AreaCard card){destroy(List.of(card));}
    public void destroy(List<AreaCard> cards){
        List<AreaCard> cardsCopy = new ArrayList<>(cards);
        cardsCopy.forEach(AreaCard::death);
    }
    public void exile(Card card){
        exile(List.of(card));
    }
    public void exile(List<Card> cards){
        msg(cards.stream().map(Card::getNameWithOwner).collect(Collectors.joining("、"))+ "从游戏中除外！");
        List<Card> cardsCopy = new ArrayList<>(cards);
        cardsCopy.forEach(card ->{
            if (card.atArea()) {
                card.ownerPlayer().getArea().remove(card);
                // 场上随从除外时，有机会发动离场时效果
                if(card instanceof AreaCard areaCard && !areaCard.getLeavings().isEmpty()){
                    msg(areaCard.getNameWithOwner() + "发动离场时效果！");
                    areaCard.getLeavings().forEach(leaving -> leaving.effect().apply());
                }
            }
            if (card.atGraveyard()) {
                card.ownerPlayer().getGraveyard().remove(card);
            }
            if (card.atHand()) {
                card.ownerPlayer().getHand().remove(card);
            }

            if(!card.getExiles().isEmpty()){
                msg(card.getNameWithOwner() + "发动除外时效果！");
                card.getExiles().forEach(exile -> exile.effect().apply());
            }
            if(card.hasKeyword("恶魔转生")){
                List<Card> totalCard = new ArrayList<>();

                totalCard.addAll(thisPlayer().getHand().stream().filter(c -> c instanceof FollowCard).toList());
                totalCard.addAll(thisPlayer().getArea().stream().filter(c -> c instanceof FollowCard).toList());
                totalCard.addAll(thisPlayer().getGraveyard().stream().filter(c -> c instanceof FollowCard).toList());
                totalCard.addAll(thisPlayer().getDeck().stream().filter(c -> c instanceof FollowCard).toList());
                totalCard.addAll(oppositePlayer().getHand().stream().filter(c -> c instanceof FollowCard).toList());
                totalCard.addAll(oppositePlayer().getArea().stream().filter(c -> c instanceof FollowCard).toList());
                totalCard.addAll(oppositePlayer().getGraveyard().stream().filter(c -> c instanceof FollowCard).toList());
                totalCard.addAll(oppositePlayer().getDeck().stream().filter(c -> c instanceof FollowCard).toList());
                Card luckyCard = Lists.randOf(totalCard);

                transform(luckyCard,card.getClass());
            }
        });
    }

    public void zeroTurn(UUID u0, UUID u1){

        PlayerInfo p0 = thisPlayer();
        PlayerDeck playerDeck0 = userDecks.get(u0);
        p0.setLeader(playerDeck0.getLeader(0, this));
        p0.setDeck(playerDeck0.getActiveDeckInstance(0, this));
        p0.setUuid(u0);
        p0.setName(userNames.get(u0));
        Collections.shuffle(p0.getDeck());

        PlayerInfo p1 = oppositePlayer();
        p1.setLeader(playerDeck0.getLeader(1, this));
        p1.setDeck(userDecks.get(u1).getActiveDeckInstance(1, this));
        p1.setUuid(u1);
        p1.setName(userNames.get(u1));
        Collections.shuffle(p1.getDeck());

        p0.draw(3);
        p1.draw(3);
        msgToThisPlayer("你的手牌:\n"+p0.describeHand());
        msgToOppositePlayer("你的手牌:\n"+p1.describeHand());

    }

    public void startTurn(){
        thisPlayer().clearCount(PLAY_NUM);
        try {
            beforeTurn();
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
            msgToThisPlayer(describeGame(thisPlayer().getUuid()));
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
        try {
            afterTurn();
        }catch (Exception e){
            e.printStackTrace();
        }
        turn += turnPlayer;// 如果是玩家1就加回合数
        turnPlayer = 1 ^ turnPlayer;
        msg("——————————");

        startTurn();
    }


    public void beforeTurn(){
        // 主战者技能重置、发动主战者效果
        Leader leader = thisPlayer().getLeader();
        leader.setCanUseSkill(true);
        leader.useEffect(EffectTiming.BeginTurn);
        leader.expireEffect();

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

            if(!areaCard.getEffectBegins().isEmpty()){
                msg(areaCard.getNameWithOwner()+"发动回合开始效果");
                areaCard.getEffectBegins().forEach(effectEnd -> {
                    effectEnd.effect().apply();
                });
            }
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
            new ArrayList<>(nameCard.values().stream()
                .filter(card -> !card.getInvocationBegins().isEmpty())
                .toList());

        // 法术卡揭示到手牌
        while(thisPlayer().getHand().size() < thisPlayer().getHandMax()){
            Optional<Card> first = canInvocation.stream().filter(card -> card instanceof SpellCard).findFirst();
            if(first.isEmpty()) break;

            // region 从牌堆召唤到手牌
            SpellCard card = (SpellCard)first.get();
            AtomicBoolean summon = new AtomicBoolean(false);
            card.getInvocationBegins().stream()
                .filter(invocationBegin -> invocationBegin.canBeTriggered().test())
                .findFirst().ifPresent(invocationBegin -> {
                    if(!summon.get()){
                        // 触发第一个效果时，揭示
                        msg(thisPlayer().getName()+"揭示了"+card.getName());
                        thisPlayer().getHand().add(card);
                        thisPlayer().getDeck().remove(card);
                        summon.set(true);
                    }
                    invocationBegin.effect().apply();
                });
            canInvocation.remove(card);
            // endregion

        }

        // 瞬念召唤到场上
        while(thisPlayer().getArea().size() < thisPlayer().getAreaMax()){
            Optional<Card> first = canInvocation.stream().filter(card -> card instanceof AreaCard).findFirst();
            if(first.isEmpty()) break;

            // region 从牌堆召唤到场上
            AreaCard card = (AreaCard)first.get();
            AtomicBoolean summon = new AtomicBoolean(false);
            card.getInvocationBegins().stream()
                .filter(invocationBegin -> invocationBegin.canBeTriggered().test())
                .findFirst().ifPresent(invocationBegin -> {
                    if(!summon.get()){
                        // 触发第一个效果时，召唤
                        msg(thisPlayer().getName()+"发动瞬念召唤");
                        thisPlayer().summon(card);
                        thisPlayer().getDeck().remove(card);
                        summon.set(true);
                    }
                    invocationBegin.effect().apply();
                });

            canInvocation.remove(card);
            // endregion
        }

    }
    public void afterTurn(){
        // 发动主战者效果
        Leader leader = thisPlayer().getLeader();
        leader.useEffect(EffectTiming.EndTurn);
        leader.expireEffect();

        // 发动回合结束效果
        List<AreaCard> areaCopy = new ArrayList<>(thisPlayer().getArea());
        areaCopy.forEach(areaCard -> {
            if(!areaCard.getEffectEnds().isEmpty()){
                msg(areaCard.getNameWithOwner()+"发动回合结束效果");
                areaCard.getEffectEnds().forEach(effectEnd -> {
                    effectEnd.effect().apply();
                });
            }
        });

        // 查找牌堆是否有瞬召卡片
        Map<String, Card> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o, (a,b)->a));
        List<Card> canInvocation =
            new ArrayList<>(nameCard.values().stream()
                .filter(card -> !card.getInvocationEnds().isEmpty())
                .toList());

        // 法术卡揭示到手牌
        while(thisPlayer().getHand().size() < thisPlayer().getHandMax()){
            Optional<Card> first = canInvocation.stream().filter(card -> card instanceof SpellCard).findFirst();
            if(first.isEmpty()) break;

            // region 从牌堆召唤到手牌
            SpellCard card = (SpellCard)first.get();
            AtomicBoolean summon = new AtomicBoolean(false);
            card.getInvocationBegins().stream()
                .filter(invocationBegin -> invocationBegin.canBeTriggered().test())
                .findFirst().ifPresent(invocationBegin -> {
                    if(!summon.get()){
                        // 触发第一个效果时，揭示
                        msg(thisPlayer().getName()+"揭示了"+card.getName());
                        thisPlayer().getHand().add(card);
                        thisPlayer().getDeck().remove(card);
                        summon.set(true);
                    }
                    invocationBegin.effect().apply();
                });
            canInvocation.remove(card);
            // endregion

        }
        // 瞬念召唤到场上
        while(thisPlayer().getArea().size() < thisPlayer().getAreaMax()){
            Optional<Card> first = canInvocation.stream().filter(card -> card instanceof AreaCard).findFirst();
            if(first.isEmpty()) break;

            // region 从牌堆召唤到场上
            AreaCard card = (AreaCard)first.get();
            AtomicBoolean summon = new AtomicBoolean(false);
            card.getInvocationBegins().stream()
                .filter(invocationBegin -> invocationBegin.canBeTriggered().test())
                .findFirst().ifPresent(invocationBegin -> {
                    if(!summon.get()){
                        // 触发第一个效果时，召唤
                        msg(thisPlayer().getName()+"发动瞬念召唤");
                        thisPlayer().summon(card);
                        thisPlayer().getDeck().remove(card);
                        summon.set(true);
                    }
                    invocationBegin.effect().apply();
                });

            canInvocation.remove(card);
            // endregion
        }
    }

    public String describeArea(UUID uuid){
        StringBuilder sb = new StringBuilder();
        PlayerInfo player = playerByUuid(uuid);
        PlayerInfo oppositePlayer = anotherPlayerByUuid(uuid);

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
                if(follow.getEquipment()!=null){
                    sb.append("装备中：").append(follow.getEquipment().getName())
                        .append("（").append(follow.getEquipment().getCountdown()).append("）\t");
                }
            }
            if("护符".equals(card.getType())){
                AmuletCard amulet = (AmuletCard) card;
                int count = amulet.getCount();
                int timer = amulet.getTimer();
                sb.append(count==-1?"-":count).append("/").append(timer==-1?"∞":timer).append("\t");
            }

            if(!card.getKeywords().isEmpty())
                sb.append(card.getKeywords());
            sb.append("\n");
        }
        sb.append("\n我方战场：\n");
        for (int i = 0; i < player.area.size(); i++) {
            Card card = player.area.get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getName()).append("\t");
            if("随从".equals(card.getType())){
                FollowCard follow = (FollowCard) card;
                if(follow.getTurnAttack() < follow.getTurnAttackMax() && (// 回合可攻击数没有打满
                    follow.getTurnAge()>0 || follow.hasKeyword("突进") || follow.hasKeyword("疾驰"))){
                    sb.append("未攻击").append("\t");
                }
                sb.append(follow.getAtk()).append("攻\t");
                sb.append(follow.getHp()).append("/").append(follow.getMaxHp()).append("血\t");
                if(follow.getEquipment()!=null){
                    sb.append("装备中：").append(follow.getEquipment().getName())
                        .append("（").append(follow.getEquipment().getCountdown()).append("）\t");
                }
            }
            if("护符".equals(card.getType())){
                AmuletCard amulet = (AmuletCard) card;
                int count = amulet.getCount();
                int timer = amulet.getTimer();
                sb.append(count==-1?"-":count).append("/").append(timer==-1?"∞":timer).append("\t");
            }

            if(!card.getKeywords().isEmpty())
                sb.append(card.getKeywords());
            sb.append("\n");
        }
        sb.append("\n");

        return sb.toString();
    }


    public String describeGame(UUID uuid){
        StringBuilder sb = new StringBuilder();
        PlayerInfo player = playerByUuid(uuid);
        PlayerInfo oppositePlayer = anotherPlayerByUuid(uuid);

        sb.append("局面信息\n");
        sb.append("剩余pp：").append(player.getPpNum()).append("\n");
        sb.append("\n");
        sb.append(oppositePlayer.name)
            .append("\t血：").append(oppositePlayer.getHp()).append("/").append(oppositePlayer.getHpMax())
            .append("\t牌：").append(oppositePlayer.deck.size())
            .append("\t墓：").append(oppositePlayer.graveyardCount)
            .append("\t手：").append(oppositePlayer.hand.size())
            .append("\n");
        sb.append(player.name)
            .append("\t血：").append(player.getHp()).append("/").append(player.getHpMax())
            .append("\t牌：").append(player.deck.size())
            .append("\t墓：").append(player.graveyardCount)
            .append("\t手：").append(player.hand.size())
            .append("\n");

        sb.append("\n");

        sb.append(describeArea(uuid));

        sb.append("我的手牌：\n").append(player.describeHand());

        return sb.toString();
    }
}
