package org.example.handler;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.game.GameInfo;
import org.example.game.PlayerInfo;
import org.example.system.WebSocketConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

import jakarta.websocket.Session;

import static org.example.system.Database.*;

@Service
@Slf4j
public class MatchHandler {

    /**
     * 加入房间进行匹配
     * */

    public void joinRoom(Session client) throws IOException {
        String room = userRoom.get(client);
        if(room != null){
            client.getBasicRemote().sendText("请不要重复进入房间！");
            return;
        }

        if(waitRoom.isBlank()){
            waitUser = client;
            waitRoom = UUID.randomUUID().toString();
            userRoom.put(client,waitRoom);
            client.getBasicRemote().sendText("进入房间（"+waitRoom+"），等待对手");

            WebSocketConfig.broadcast("【全体】有人正在匹配对战，点击匹配以尝试加入该对战！");
        }else {
            userRoom.put(client,waitRoom);
            client.getBasicRemote().sendText( "进入房间（"+waitRoom+"）");
            client.getBasicRemote().sendText(
                "匹配成功！ 【"+userNames.get(client)+"】vs【"+userNames.get(waitUser)+"】");
            waitUser.getBasicRemote().sendText(
                "匹配成功！ 【"+userNames.get(waitUser)+"】vs【"+userNames.get(client)+"】");

            WebSocketConfig.broadcast("【全体】一场对战已经匹配成功！");


            // region 不需要准备直接开始
            // 比赛开始
            GameInfo info = new GameInfo(waitRoom);

            // 初始化游戏，先匹配的先手
            info.zeroTurn(waitUser,client);

            roomGame.put(waitRoom,info);
            // endregion 不需要准备直接开始

            waitRoom = "";
            waitUser = null;

        }
    }


    public void leave(Session client) throws IOException {
        String room = userRoom.get(client);
        if(room==null){
            client.getBasicRemote().sendText("你不在任何房间中");
            return;
        }
        GameInfo info = roomGame.get(room);
        if(info!=null){
            PlayerInfo player = info.playerBySession(client);
            PlayerInfo enemy = info.anotherPlayerBySession(client);
            info.msg(player.getName() + "离开了游戏！");
            info.gameset(enemy);
            return;
        }
        userRoom.remove(client);
        client.getBasicRemote().sendText("离开房间成功");
        // 释放资源
        roomGame.remove(room);
        if(client.equals(waitUser) || room.equals(waitRoom) ){
            waitRoom = "";
            waitUser = null;
            WebSocketConfig.broadcast("【全体】匹配中的玩家已经退出了！");
        }
        // 退出房间

    }


}

