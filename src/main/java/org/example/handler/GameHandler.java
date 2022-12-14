package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.card.Card;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.example.system.Database.*;

@Service
@ConditionalOnClass(SocketIOServer.class)
@Slf4j
public class GameHandler {
    @Autowired
    SocketIOServer socketIOServer;

    @Autowired
    Gson gson;

    @OnEvent(value = "swap")
    public void checkDeck(SocketIOClient client, String msg) {
        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        GameInfo.PlayerInfo player = info.playerByUuid(me);
        // endregion

        // region 交换
        Set<Integer> indexs = new HashSet<>();
        for (String index : msg.split("\\s+")) {
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
            indexs.add(indexI);
        }
        if(player.getStep().get()!=-1){
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

        player.getStep().set(0);
        if(info.anotherPlayerByUuid(me).getStep().get()==0){
            String turnPlayerName = info.getPlayerInfos()[info.getTurnPlayer()].getName();
            // 两名玩家都换完了，开始游戏
            socketIOServer.getRoomOperations(room).sendEvent("receiveMsg", "双方均交换完成，游戏开始！由【"+turnPlayerName+"】先出牌。");

        }
    }
}
