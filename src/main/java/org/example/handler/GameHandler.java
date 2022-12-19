package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.GameInfo;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.example.system.Database.roomGame;
import static org.example.system.Database.userNames;

@Service
@ConditionalOnClass(SocketIOServer.class)
@Slf4j
public class GameHandler {
    @Autowired
    SocketIOServer socketIOServer;

    @Autowired
    Gson gson;

    @OnEvent(value = "swap")
    public void swap(SocketIOClient client, String msg) {
        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.playerByUuid(me);
        // endregion

        // region 交换
        Set<Integer> indexs = new HashSet<>();
        for (String index : msg.split("\\s+")) {
            if(index.isEmpty()) continue;
            Integer indexI;
            try {
                indexI = Integer.valueOf(index);
            }catch (Exception e){
                indexI = -1;
            }
            if(indexI<1 || indexI>3){
                socketIOServer.getClient(me).sendEvent("receiveMsg", "输入序号错误:"+index);
                return;
            }
            indexs.add(indexI-1);//这里转成下标
        }
        if(player.getStep()!=-1){
            socketIOServer.getClient(me).sendEvent("receiveMsg", "已经过了换牌步骤!");
            return;
        }
        if(indexs.isEmpty()){
            socketIOServer.getRoomOperations(room).sendEvent("receiveMsg", name +"交换了0张卡牌");
        }else{
            List<Card> hand = player.getHand();
            List<Card> back = new ArrayList<>();
            for (Integer index : indexs) {
                back.add(hand.get(index));
            }
            player.draw(back.size());
            player.back(back);
            socketIOServer.getRoomOperations(room).sendEvent("receiveMsg", name +"交换了"+back.size()+"张卡牌");
        }
        // endregion

        socketIOServer.getClient(me).sendEvent("receiveMsg", "交换后的手牌:\n"+player.describeHand());

        player.setStep(0);
        if(info.anotherPlayerByUuid(me).getStep()==0){
            String turnPlayerName = info.getPlayerInfos()[info.getTurnPlayer()].getName();
            // 两名玩家都换完了，开始游戏
            socketIOServer.getRoomOperations(room).sendEvent("receiveMsg", "双方均交换完成，游戏开始！由【"+turnPlayerName+"】先攻。");

            info.startTurn();
        }
    }

    @OnEvent(value = "play")
    public void play(SocketIOClient client, String msg) {
        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.playerByUuid(me);
        // endregion

        if("end".equals(msg)){
            info.endTurn();
            return;
        }
        if(msg.isBlank()){
            socketIOServer.getClient(me).sendEvent("receiveMsg", "打出卡牌：play <手牌序号>；结束回合：play end");
            return;
        }

        String[] split = msg.split("\\s+");

        Integer indexI;
        try {
            indexI = Integer.valueOf(split[0]);
        }catch (Exception e){
            indexI = -1;
        }
        if(indexI <= 0 || indexI > player.getDeck().size()){
            socketIOServer.getClient(me).sendEvent("receiveMsg", "输入手牌序号错误:"+split[0]);
            return;
        }

        Card card = player.getDeck().get(indexI - 1);
        List<GameObj> targetable = card.targetable();
        // 只有一个参数
        if(split.length == 1){
            if(targetable.size()==0){
                card.play(targetable);
            }else {
                StringBuilder sb = new StringBuilder();
                sb.append("你需要指定目标以使用该卡牌：play <手牌序号> <目标序号>\n")
                    .append("可指定的目标：\n");
                for (int i = 0; i < targetable.size(); i++) {
                    sb.append("【").append(i+1).append("】\t");

                    GameObj gameObj = targetable.get(i);
                    if(gameObj instanceof Leader leader){
                        sb.append(leader.isMe()?"我方主战者":"敌方主战者");
                    }else if (gameObj instanceof Card targetCard){
                        // 卡牌属于哪方
                        PlayerInfo ownerPlayer = targetCard.ownerPlayer();
                        sb.append(ownerPlayer == player ? "我方\t":"敌方\t")
                            .append(targetCard.getName()).append("\t");
                        if(gameObj instanceof FollowCard followCard){
                            sb.append("随从\t")
                                .append(followCard.atk).append("/").append(followCard.hp);
                        }else if(gameObj instanceof AmuletCard amuletCard){
                            sb.append("护符\t")
                                .append(amuletCard.count).append("/").append(amuletCard.timer);
                        }
                    }
                }

            }
        }else{
            // 输入多个参数
            if(targetable.size()==0) {
                socketIOServer.getClient(me).sendEvent("receiveMsg", "无法为该卡牌指定目标！");
                return;
            }else {
                List<GameObj> targets = new ArrayList<>();
                for (String targetS : split) {
                    Integer targetI;
                    try {
                        targetI = Integer.valueOf(targetS);
                    }catch (Exception e){
                        targetI = 0;
                    }

                    GameObj target = targetable.get(targetI - 1);
                    if(target!=null){
                        targets.add(target);
                    }
                }
                // 指定目标数量错误
                if (targets.size() != card.targetNum()) {
                    socketIOServer.getClient(me).sendEvent("receiveMsg", "指定目标数量错误，应为："+card.targetNum());
                    return;
                }
                card.play(targets);
            }

        }


    }


}
