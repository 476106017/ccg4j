package org.example.game;

import com.corundumstudio.socketio.SocketIOServer;
import lombok.Data;
import org.example.card.Card;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.system.Database.schedule;

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
        this.turn = 0;
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
            int hp = thisPlayer().hp;
            hp -= damage;
            if(hp < 0){
                gameset();
            }
        }else {
            int hp = oppositePlayer().hp;
            hp -= damage;
            if (hp < 0) {
                gameset();
            }
        }
    }

    public void startTurn(){
        beforeTurn();
        msg("第" + turn + "回合：" + thisPlayer().getName()+"的回合，有" + thisPlayer().ppNum + "pp，请出牌");
        if(rope.isDone()){// 前一个绳子烧完了，就只给十秒
            rope = schedule.scheduleWithFixedDelay(this::endTurn, 0, 10, TimeUnit.SECONDS);
            msg("只有10秒时间出牌！");
        }else{
            rope = schedule.scheduleWithFixedDelay(this::endTurn, 0, 1, TimeUnit.MINUTES);
            msg("请在60秒时间内出牌！");
        }
    }

    public void endTurn(){
        msg(thisPlayer().getName()+"的回合结束");
        afterTurn();
        if(turnPlayer==0){
            turnPlayer = 1;
        }else {
            turnPlayer = 0;
            turn++;
        }
        thisPlayer().ppMax++;
        thisPlayer().ppNum = thisPlayer().ppMax;
        startTurn();
    }


    public void beforeTurn(){
        Map<String, Card> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o, (a,b)->a));

        while(thisPlayer().getArea().size()<5){
            Optional<Card> first = nameCard.values().stream().filter(Card::canInstantBegin).findFirst();
            if (first.isEmpty()) {
                break;
            }
            // region 从牌堆召唤到场上
            Card card = first.get();
            thisPlayer().getArea().add(card);
            thisPlayer().getDeck().remove(card);
            card.afterInstantBegin();
            msg(thisPlayer().getName()+"瞬念召唤了"+card.getName());
            // endregion
        }

    }
    public void afterTurn(){
        Map<String, Card> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o));

        while(thisPlayer().getArea().size()<5){
            Optional<Card> first = nameCard.values().stream().filter(Card::canInstantEnd).findFirst();
            if (first.isEmpty()) {
                break;
            }
            // region 从牌堆召唤到场上
            Card card = first.get();
            thisPlayer().getArea().add(card);
            thisPlayer().getDeck().remove(card);
            card.afterInstantEnd();
            msg(thisPlayer().getName()+"瞬念召唤了"+card.getName());
            // endregion
        }
    }
}
