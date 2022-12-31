package org.example.game;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.Getter;
import lombok.Setter;
import org.example.card.*;
import org.example.constant.EffectTiming;
import org.example.system.Lists;
import org.springframework.beans.BeanUtils;

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

    public void transform(Card fromCard, Card toCard){
        msg(fromCard.getNameWithOwnerWithPlace()+ "已变身成了" + toCard.getName());
        if(fromCard.atArea()){
            List<AreaCard> area = fromCard.ownerPlayer().getArea();
            int index = area.indexOf(fromCard);
            area.remove(index);
            if (toCard instanceof AreaCard areaCard) {
                area.add(index, areaCard);
            } else {
                msg(toCard.getNameWithOwner()+ "无法留在战场而被除外！");
                exile(toCard);
            }
        }else {
            List<Card> where = fromCard.where();
            int index = where.indexOf(fromCard);
            where.remove(index);
            where.add(index,toCard);
        }
    }
    public void exile(Card card){
        exile(List.of(card));
    }
    public void exile(List<Card> cards){
        if(cards.isEmpty())return;
        msg(cards.stream().map(Card::getNameWithOwner).collect(Collectors.joining("、"))+ "从游戏中除外！");
        cards.forEach(card ->{
            if(card.where()==null)return;

            // 场上卡除外时，有机会发动离场时效果
            if (card.atArea() && card instanceof AreaCard areaCard){
                if(!areaCard.getLeavings().isEmpty()) {
                    msg(areaCard.getNameWithOwner() + "发动离场时效果！");
                    areaCard.getLeavings().forEach(leaving -> leaving.effect().apply());
                }
            }
            // 随从除外时，装备也除外
            if(card instanceof FollowCard followCard && followCard.equipped()){
                exile(followCard.getEquipment());
            }

            card.remove();

            if(!card.getExiles().isEmpty()){
                msg(card.getNameWithOwner() + "发动除外时效果！");
                card.getExiles().forEach(exile -> exile.effect().apply());
            }
            if(card.hasKeyword("恶魔转生")){
                List<Card> totalCard = new ArrayList<>();

                totalCard.addAll(thisPlayer().getHand().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(thisPlayer().getArea().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(thisPlayer().getGraveyard().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(thisPlayer().getDeck().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(oppositePlayer().getHand().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(oppositePlayer().getArea().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(oppositePlayer().getGraveyard().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(oppositePlayer().getDeck().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());

                if(totalCard.isEmpty()){
                    msg("游戏中只剩下恶魔牌，"+card.getNameWithOwner()+"已经无法转生");
                    return;
                }
                Card luckyCard = Lists.randOf(totalCard);



                Card newCard = card.createCard(card.getClass());
                transform(luckyCard,newCard);
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
        msg("游戏开始，请选择3张手牌交换");
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
                rope = roomSchedule.get(getRoom()).schedule(this::endTurnOfTimeout, 30, TimeUnit.SECONDS);
                msg("倒计时30秒！");
            }else{
                rope = roomSchedule.get(getRoom()).schedule(this::endTurnOfTimeout, 300, TimeUnit.SECONDS);
                msg("倒计时300秒！");
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
        thisPlayer().getAreaCopy().forEach(areaCard -> {
            if(!areaCard.atArea())return;

            if(!areaCard.getEffectBegins().isEmpty()){
                msg(areaCard.getNameWithOwner()+"发动回合开始效果");
                areaCard.getEffectBegins().forEach(effectEnd -> {
                    effectEnd.effect().apply();
                });
            }
            if(!areaCard.atArea())return;


            if(areaCard instanceof FollowCard followCard && followCard.equipped()){
                EquipmentCard equipment = followCard.getEquipment();
                List<AreaCard.Event.EffectBegin> effectBegins = equipment.getEffectBegins();
                if(!effectBegins.isEmpty()){
                    msg(areaCard.getNameWithOwner() + "发动其装备"+equipment.getName()+"的回合开始效果");
                    effectBegins.forEach(effectEnd -> effectEnd.effect().apply());
                }
            }
            if(!areaCard.atArea())return;

            if(areaCard instanceof FollowCard followCard){
                int turnAgePlus = followCard.getTurnAge() + 1;
                if(turnAgePlus>0){// 可能有随从会需要准备多个回合，还是判断下
                    msg(followCard.getNameWithOwner() + "可以攻击了！");
                }
                followCard.setTurnAge(turnAgePlus);

                followCard.setTurnAttack(0);
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

        // 查找牌堆是否有瞬召卡牌，同名字卡牌各取一张
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
        thisPlayer().getAreaCopy().forEach(areaCard -> {
            if (areaCard.atArea())
                if (!areaCard.getEffectEnds().isEmpty()) {
                    msg(areaCard.getNameWithOwner() + "发动回合结束效果");
                    areaCard.getEffectEnds().forEach(effectEnd -> {
                        effectEnd.effect().apply();
                    });
                }
            if(!areaCard.atArea())return;
            if(areaCard instanceof FollowCard followCard && followCard.equipped()){
                EquipmentCard equipment = followCard.getEquipment();
                List<AreaCard.Event.EffectEnd> effectEnds = equipment.getEffectEnds();
                if(!effectEnds.isEmpty()){
                    msg(areaCard.getNameWithOwner() + "发动其装备"+equipment.getName()+"的回合结束效果");
                    effectEnds.forEach(effectEnd -> effectEnd.effect().apply());
                }
            }
        });

        // 查找牌堆是否有瞬召卡牌
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

        sb.append("【战场信息】\n");
        sb.append("敌方战场：\n");
        for (int i = 0; i < oppositePlayer.getArea().size(); i++) {
            Card card = oppositePlayer.getArea().get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getName()).append("\t");
            if("随从".equals(card.getType())){
                FollowCard follow = (FollowCard) card;
                sb.append(follow.getAtk()).append("/").append(follow.getHp())
                    .append("\t").append(follow.getMaxHp()==follow.getHp()?"满":"残").append("\t");
                if(follow.getEquipment()!=null){
                    sb.append("装备中：").append(follow.getEquipment().getName());
                    if(follow.getEquipment().getCountdown()!=-1)
                        sb.append("（").append(follow.getEquipment().getCountdown()).append("）");
                    sb.append("\t");
                }
            }
            if("护符".equals(card.getType())){
                AmuletCard amulet = (AmuletCard) card;
                if(amulet.getTimer()>0){
                    sb.append("倒数：").append(amulet.getTimer()).append("\t");
                }
            }

            if(!card.getKeywords().isEmpty())
                sb.append(card.getKeywords());
            sb.append("\n");
        }
        sb.append("\n我方战场：\n");
        for (int i = 0; i < player.getArea().size(); i++) {
            Card card = player.getArea().get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getName()).append("\t");
            if("随从".equals(card.getType())){
                FollowCard follow = (FollowCard) card;
                if(follow.getTurnAttack() < follow.getTurnAttackMax() && (// 回合可攻击数没有打满
                    follow.getTurnAge()>0 || follow.hasKeyword("突进") || follow.hasKeyword("疾驰"))){
                    sb.append("未攻击").append("\t");
                }
                sb.append(follow.getAtk()).append("/").append(follow.getHp())
                    .append("\t").append(follow.getMaxHp()==follow.getHp()?"满":"残").append("\t");
                if(follow.getEquipment()!=null){
                    sb.append("装备中：").append(follow.getEquipment().getName());
                    if(follow.getEquipment().getCountdown()!=-1)
                        sb.append("（").append(follow.getEquipment().getCountdown()).append("）");
                    sb.append("\t");
                }
            }
            if("护符".equals(card.getType())){
                AmuletCard amulet = (AmuletCard) card;
                if(amulet.getTimer()>0){
                    sb.append("倒数：").append(amulet.getTimer()).append("\t");
                }
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
        sb.append(player.describePPNum());
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
