package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.card.ccg.nemesis.Yuwan;
import org.example.constant.DeckPreset;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.example.game.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

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
        String ip = client.getHandshakeData().getHttpHeaders().get("X-Forwarded-For");
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
        log.info("客户端" + uuid + "建立websocket连接成功,用户名："+name);
        // region TODO 先用默认牌组
        PlayerDeck playerDeck = new PlayerDeck();
        playerDeck.setLeaderClass(Yuwan.class);
        playerDeck.getActiveDeck().addAll(DeckPreset.decks.get("test"));
        userDecks.put(uuid, playerDeck);
        // endregion TODO 先用默认牌组
        userNames.put(uuid,name);
        socketIOServer.getClient(uuid).sendEvent("receiveMsg", name+"（"+uuid+"）登录成功！");

        socketIOServer.getBroadcastOperations().sendEvent("receiveMsg",
            "【系统广播】"+name+"（"+ip+"）登录了游戏！" +
                "当前在线："+socketIOServer.getAllClients().size()+"人");
    }


    @OnDisconnect
    public void onDisConnect(SocketIOClient client) {
        UUID me = client.getSessionId();
        String ip = client.getHandshakeData().getHttpHeaders().get("X-Forwarded-For");
        String name = userNames.get(me);
        userNames.remove(me);
        socketIOServer.getBroadcastOperations().sendEvent("receiveMsg",
            "【系统广播】"+name+"（"+ip+"）退出了游戏！");
        String room = userRoom.get(me);
        if(room==null)return;

        GameInfo info = roomGame.get(room);
        if(info!=null){
            PlayerInfo player = info.playerByUuid(me);
            PlayerInfo enemy = info.anotherPlayerByUuid(me);
            info.msg(player.getName() + "已断开连接！");
            info.gameset(enemy);
            return;
        }
        client.leaveRoom(room);
        // 释放资源
        roomReadyMatch.remove(room);
        roomGame.remove(room);
        userRoom.remove(me);
        if(me.equals(waitUser) || room.equals(waitRoom) ){
            waitRoom = "";
            waitUser = null;
        }
        // 退出房间
        log.info("客户端" + client.getSessionId() + "断开websocket连接成功");
    }

    /**
     * 加入房间进行匹配
     * */
    @OnEvent(value = "jr")
    public void joinRoom(SocketIOClient client) throws InterruptedException {
        UUID me = client.getSessionId();
        String room = userRoom.get(me);
        if(room != null){
            socketIOServer.getClient(me).sendEvent("receiveMsg", "请不要重复进入房间！");
            return;
        }

        if(waitRoom.isBlank()){
            waitUser = me;
            waitRoom = UUID.randomUUID().toString();
            client.joinRoom(waitRoom);
            userRoom.put(me,waitRoom);
            socketIOServer.getClient(me).sendEvent("receiveMsg", "进入房间（"+waitRoom+"），等待对手");
        }else {
            client.joinRoom(waitRoom);
            userRoom.put(me,waitRoom);
            socketIOServer.getClient(me).sendEvent("receiveMsg", "进入房间（"+waitRoom+"）");
            socketIOServer.getClient(me).sendEvent("receiveMsg",
                "匹配成功！ 【"+userNames.get(me)+"】vs【"+userNames.get(waitUser)+"】");
            socketIOServer.getClient(waitUser).sendEvent("receiveMsg",
                "匹配成功！ 【"+userNames.get(waitUser)+"】vs【"+userNames.get(me)+"】");

            // region 不需要准备直接开始
            // 比赛开始
            GameInfo info = new GameInfo(socketIOServer,waitRoom);

            // 初始化游戏，先匹配的先手
            info.zeroTurn(waitUser,me);

            roomGame.put(waitRoom,info);
            // endregion 不需要准备直接开始

            waitRoom = "";
            waitUser = null;

        }
    }

    @OnEvent(value = "leave")
    public void leave(SocketIOClient client, String msg){

        UUID me = client.getSessionId();
        String room = userRoom.get(me);
        if(room==null){
            client.sendEvent("receiveMsg","你不在任何房间中");
            return;
        }
        GameInfo info = roomGame.get(room);
        if(info!=null){
            PlayerInfo player = info.playerByUuid(me);
            PlayerInfo enemy = info.anotherPlayerByUuid(me);
            info.msg(player.getName() + "离开了游戏！");
            info.gameset(enemy);
            return;
        }
        userRoom.remove(me);
        client.leaveRoom(room);
        client.sendEvent("receiveMsg","离开房间成功");
        // 释放资源
        roomReadyMatch.remove(room);
        roomGame.remove(room);
        if(me.equals(waitUser) || room.equals(waitRoom) ){
            waitRoom = "";
            waitUser = null;
        }
        // 退出房间

    }


}

