package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.card.Card;
import org.example.card.follow.Bahamut;
import org.example.card.follow.FairyWhisperer;
import org.example.card.spell.DarkSnare;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.example.system.RoomMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
        if (name == null) {
            System.err.println("客户端" + uuid + "建立websocket连接失败，token不能为null");
            client.disconnect();
            return;
        }
        System.out.println("客户端" + uuid + "建立websocket连接成功,用户名："+name);
        // region TODO 先由临时玩家游玩，直接拥有全部卡牌
        PlayerDeck playerDeck = new PlayerDeck();
        List<Card> activeDeck = playerDeck.getActiveDeck();
        for (int i = 0; i < 3; i++) {
            activeDeck.add(new Bahamut());
            activeDeck.add(new FairyWhisperer());
            activeDeck.add(new DarkSnare());

        }
        userDecks.put(uuid, playerDeck);
        // endregion TODO 先由临时玩家游玩，直接拥有全部卡牌
        userNames.put(uuid,name);
    }


    @OnDisconnect
    public void onDisConnect(SocketIOClient client) {
        System.out.println("客户端" + client.getSessionId() + "断开websocket连接成功");
        userNames.remove(client.getSessionId());
    }

    /**
     * 加入房间进行匹配
     * */
    @OnEvent(value = "joinRoom")
    public void onTestJoinRoomEvent(SocketIOClient client) {
        UUID sessionId = client.getSessionId();
        String oldRoom = userRoom.get(sessionId);
        client.leaveRoom(oldRoom);

        // 找到房间另一个玩家
        Optional<UUID> waitRoomUser = roomUser.get(waitRoom)
            .stream().filter(p->!sessionId.equals(p)).findAny();
        if(waitRoom.isBlank()){
            waitRoom = UUID.randomUUID().toString();
            socketIOServer.getClient(sessionId).sendEvent("joinRoomResp", "waiting");
        }else {
            waitRoom = "";
            socketIOServer.getClient(sessionId).sendEvent("joinRoomResp", "matched");
            socketIOServer.getClient(waitRoomUser.get()).sendEvent("joinRoomResp", "matched");
        }
        client.joinRoom(waitRoom);
        userRoom.put(sessionId,waitRoom);
    }

    /**
     * 准备比赛
     * */
    @OnEvent(value = "readyMatch")
    public void onTestRoomEvent(SocketIOClient client,  RoomMsg data) {
        UUID uuid = client.getSessionId();
        String name = userNames.get(uuid);
        String room = data.getRoom();
        socketIOServer.getRoomOperations(room).sendEvent("readyMatchResp", name + "已经就绪");

        String room1 = userRoom.get(uuid);// db中玩家所处房间
        if(!room1.equals(room)){
            socketIOServer.getRoomOperations(room).sendEvent("readyMatchResp", "房间异常解散");
            socketIOServer.getRoomOperations(room1).sendEvent("readyMatchResp", "房间异常解散");
            client.leaveRoom(room);
            client.leaveRoom(room1);
            return;
        }
        UUID readyMatch = roomReadyMatch.get(room);
        if(readyMatch != null){
            roomReadyMatch.put(room, uuid);
        }else {
            // 比赛开始
            GameInfo info = new GameInfo();
            info.thisPlayer().setDeck(
                userDecks.get(readyMatch).getActiveDeck());
            info.thisPlayer().setUuid(readyMatch);
            info.thisPlayer().setName(userNames.get(readyMatch));
            info.oppositePlayer().setDeck(
                userDecks.get(uuid).getActiveDeck());
            info.oppositePlayer().setUuid(uuid);
            info.oppositePlayer().setName(name);
            roomGame.put(room,info);
            socketIOServer.getRoomOperations(room).sendEvent("readyMatchResp", "比赛开始");

        }
    }

    /**
     * 发送广播消息
     * */
    @OnEvent(value = "broadcastChat")
    public void broadcastChat(SocketIOClient client, String data) {
        String name = userNames.get(client.getSessionId());
        socketIOServer.getBroadcastOperations().sendEvent("broadcastChat", "【全体】" +name+ "："+ data);
    }

    /**
     * 发送房间消息
     * */
    @OnEvent(value = "roomChat")
    public void roomChat(SocketIOClient client, RoomMsg data) {
        String name = userNames.get(client.getSessionId());
        socketIOServer.getRoomOperations(data.getRoom()).sendEvent("roomChat", "【房间】" +name+ "："+ data.getMsg());
    }


}

