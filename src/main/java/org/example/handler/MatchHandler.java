package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.card.Card;
import org.example.card.fairy.amulet.ForestSymphony;
import org.example.card.fairy.follow.FairyWhisperer;
import org.example.card.fairy.spell.ForestGenesis;
import org.example.card.nemesis.spell.CalamitysGenesis;
import org.example.card.neutral.follow.Bahamut;
import org.example.card.neutral.follow.TravelerGoblin;
import org.example.card.neutral.spell.DarkSnare;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.example.system.Database.*;

@Service
@ConditionalOnClass(SocketIOServer.class)
@Slf4j
public class MatchHandler {
    @Autowired
    SocketIOServer socketIOServer;

    @Autowired
    Gson gson;


    @OnConnect
    public void onConnect(SocketIOClient client) {
        String name = client.getHandshakeData().getSingleUrlParam("name");
        UUID uuid = client.getSessionId();

        String oldName = userNames.get(uuid);
        if(Strings.isNotBlank(oldName)){
            // 已经注册过
            return;
        }
        if (name == null) {
            System.err.println("客户端" + uuid + "建立websocket连接失败，token不能为null");
            client.disconnect();
            return;
        }
        System.out.println("客户端" + uuid + "建立websocket连接成功,用户名："+name);
        // region TODO 先由临时玩家游玩，直接拥有全部卡牌
        PlayerDeck playerDeck = new PlayerDeck();
        List<Class<? extends Card>> activeDeck = playerDeck.getActiveDeck();
        for (int i = 0; i < 3; i++) {
            activeDeck.add(Bahamut.class);
            activeDeck.add(FairyWhisperer.class);
            activeDeck.add(DarkSnare.class);
            activeDeck.add(TravelerGoblin.class);
            activeDeck.add(CalamitysGenesis.class);
            activeDeck.add(ForestGenesis.class);
            activeDeck.add(ForestSymphony.class);

        }
        userDecks.put(uuid, playerDeck);
        // endregion TODO 先由临时玩家游玩，直接拥有全部卡牌
        userNames.put(uuid,name);
        socketIOServer.getClient(uuid).sendEvent("receiveMsg", name+"（"+uuid+"）登录成功！");
    }


    @OnDisconnect
    public void onDisConnect(SocketIOClient client) {
        System.out.println("客户端" + client.getSessionId() + "断开websocket连接成功");
    }

    /**
     * 加入房间进行匹配
     * */
    @OnEvent(value = "jr")
    public void joinRoom(SocketIOClient client) throws InterruptedException {
        UUID me = client.getSessionId();
        Set<String> allRooms = client.getAllRooms();
        if(allRooms.size()>1){
            socketIOServer.getClient(me).sendEvent("receiveMsg", "请不要重复进入房间！");
            return;
        }

        if(waitRoom.isBlank()){
            waitUser = me;
            waitRoom = UUID.randomUUID().toString();
            client.joinRoom(waitRoom);
            socketIOServer.getClient(me).sendEvent("receiveMsg", "进入房间（"+waitRoom+"），等待对手");
        }else {
            client.joinRoom(waitRoom);
            socketIOServer.getClient(me).sendEvent("receiveMsg", "进入房间（"+waitRoom+"）");
            socketIOServer.getClient(me).sendEvent("receiveMsg",
                "匹配成功！ 【"+userNames.get(me)+"】vs【"+userNames.get(waitUser)+"】");
            socketIOServer.getClient(waitUser).sendEvent("receiveMsg",
                "匹配成功！ 【"+userNames.get(waitUser)+"】vs【"+userNames.get(me)+"】");
            waitRoom = "";
            waitUser = null;
        }
    }

    /**
     * 准备比赛
     * */
    @OnEvent(value = "zb")
    public void ready(SocketIOClient client,  String data) {
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = client.getAllRooms().stream().filter(s->!s.isEmpty()).findFirst().get();
        if(roomGame.get(room)!=null){
            socketIOServer.getClient(me).sendEvent("receiveMsg", "比赛已经开始了！");
            return;
        }

        UUID readyMatch = roomReadyMatch.get(room);
        if(readyMatch == null){
            roomReadyMatch.put(room, me);
            socketIOServer.getRoomOperations(room).sendEvent("receiveMsg", name + "已经就绪!");
        }else if(readyMatch == me) {
            socketIOServer.getClient(me).sendEvent("receiveMsg", "你已经就绪了！");
        }else{
            // 比赛开始
            GameInfo info = new GameInfo(socketIOServer,room);

            // 初始化游戏
            info.zeroTurn(readyMatch,me);

            roomGame.put(room,info);
        }
    }


}

