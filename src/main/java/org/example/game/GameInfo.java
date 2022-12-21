package org.example.game;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.Data;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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
        this.playerInfos[0] = new PlayerInfo();
        this.playerInfos[1] = new PlayerInfo();

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

    public void gameset(){
        // TODO 结束游戏，需要通知到双方玩家
        gameset = true;
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

    public void destroy(PlayerInfo playerInfo, List<Card> cards){
        playerInfo.getGraveyard().addAll(cards);
        playerInfo.getArea().removeAll(cards);
    }

    public void damageLeader(Leader leader,int damage){
        if(thisPlayer().getLeader().equals(leader)){
            int hp = thisPlayer().getHp();
            thisPlayer().setHp(hp - damage);
            msg(thisPlayer().getName()+"的主战者受到"+damage+"点伤害！（剩余"+thisPlayer().getHp()+"点生命值）");
            if(hp < 0){
                gameset();
            }
        }else {
            int hp = oppositePlayer().getHp();
            oppositePlayer().setHp(hp - damage);
            msg(oppositePlayer().getName()+"的主战者受到"+damage+"点伤害！（剩余"+oppositePlayer().getHp()+"点生命值）");
            if (hp < 0) {
                gameset();
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
            msg("第" + turn + "回合：" + thisPlayer().getName()+"的回合，有" + thisPlayer().ppNum + "pp");
            // 前一个绳子烧完了，就只给十秒
            oppositePlayer().setShortRope(rope!=null && rope.isDone());

            if(thisPlayer().isShortRope()){
                rope = schedule.schedule(this::endTurnOfTimeout, 10, TimeUnit.SECONDS);
                msg("倒计时10秒！");
            }else{
                rope = schedule.schedule(this::endTurnOfTimeout, 600, TimeUnit.SECONDS);
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

        // 场上随从驻场回合+1、攻击次数清零
        thisPlayer().getArea().forEach(areaCard -> {
            if(areaCard instanceof FollowCard followCard){
                int turnAgePlus = followCard.getTurnAge() + 1;
                followCard.setTurnAge(turnAgePlus);

                followCard.setTurnAttack(0);
            }
        });

        // 查找牌堆是否有瞬召卡片
        Map<String, Card> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o, (a,b)->a));

        while(thisPlayer().getArea().size()<5){
            Optional<Card> first = nameCard.values().stream()
                .filter(card -> card instanceof AreaCard  areaCard && areaCard.canInstantBegin()).findFirst();
            if (first.isEmpty()) {
                break;
            }
            // region 从牌堆召唤到场上
            AreaCard card = (AreaCard)first.get();
            thisPlayer().getArea().add(card);
            thisPlayer().getDeck().remove(card);
            card.afterInstantBegin();
            msg(thisPlayer().getName()+"瞬念召唤了"+card.getName());
            // endregion
        }

    }
    public void afterTurn(){
        Map<String, Card> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o, (a,b)->a));

        while(thisPlayer().getArea().size()<5){
            Optional<Card> first = nameCard.values().stream()
                .filter(card -> card instanceof AreaCard  areaCard && areaCard.canInstantEnd()).findFirst();
            if (first.isEmpty()) {
                break;
            }
            // region 从牌堆召唤到场上
            AreaCard card = (AreaCard)first.get();
            thisPlayer().getArea().add(card);
            thisPlayer().getDeck().remove(card);
            card.afterInstantEnd();
            msg(thisPlayer().getName()+"瞬念召唤了"+card.getName());
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
