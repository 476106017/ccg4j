package org.example.endpoint.handler;

import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.game.GameInfo;
import org.example.game.PlayerInfo;
import org.example.system.GameStateService;
import org.example.system.WebSocketConfig;
import org.example.system.util.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
public class MatchHandler {

    @Autowired
    private GameStateService gameStateService;
    @Autowired
    private WebSocketConfig webSocketConfig;

    /**
     * 加入房间进行匹配
     * */

    public void joinRoom(Session client) throws IOException {
        String room = gameStateService.getUserRoom().get(client);
        if(room != null){
            Msg.warn(client,"请不要重复进入房间！");
            return;
        }

        if(gameStateService.getWaitRoom().isBlank()){
            gameStateService.setWaitUser(client);
            gameStateService.setWaitRoom(UUID.randomUUID().toString());
            gameStateService.getUserRoom().put(client,gameStateService.getWaitRoom());
            Msg.send(client,"waitRoom",gameStateService.getWaitRoom());

            webSocketConfig.broadcast("【全体】有人正在匹配对战，点击匹配以尝试加入该对战！");
        }else {
            gameStateService.getUserRoom().put(client,gameStateService.getWaitRoom());
            Msg.send(client,"进入房间（"+gameStateService.getWaitRoom()+"）");
            Msg.send(client,"匹配成功！ 【"+gameStateService.getUserNames().get(client)+"】vs【"+gameStateService.getUserNames().get(gameStateService.getWaitUser())+"】");
            Msg.send(gameStateService.getWaitUser(),"匹配成功！ 【"+gameStateService.getUserNames().get(gameStateService.getWaitUser())+"】vs【"+gameStateService.getUserNames().get(client)+"】");

            webSocketConfig.broadcast("【全体】一场对战已经匹配成功！");


            // region 不需要准备直接开始
            // 比赛开始
            GameInfo info = new GameInfo(gameStateService.getWaitRoom(), this.gameStateService);

            // 初始化游戏，先匹配的先手
            PlayerDeck deck0 = gameStateService.getUserDecks().get(gameStateService.getWaitUser());
            String name0 = gameStateService.getUserNames().get(gameStateService.getWaitUser());
            PlayerDeck deck1 = gameStateService.getUserDecks().get(client);
            String name1 = gameStateService.getUserNames().get(client);
            info.zeroTurn(deck0, name0, gameStateService.getWaitUser(), deck1, name1, client);

            gameStateService.getRoomGame().put(gameStateService.getWaitRoom(),info);
            // endregion 不需要准备直接开始

            gameStateService.setWaitRoom("");
            gameStateService.clearWaitUser();
        }
    }


    public void leave(Session client) throws IOException {
        String room = gameStateService.getUserRoom().get(client);
        if(room==null){
            Msg.send(client,"你不在任何房间中");
            return;
        }
        GameInfo info = gameStateService.getRoomGame().get(room);
        if(info!=null){
            PlayerInfo player = info.playerBySession(client);
            PlayerInfo enemy = info.anotherPlayerBySession(client);
            info.msg(player.getName() + "离开了游戏！");
            info.gameset(enemy);
            return;
        }
        gameStateService.getUserRoom().remove(client);
        Msg.send(client,"离开房间成功");
        // 释放资源
        gameStateService.getRoomGame().remove(room);
        if(client.equals(gameStateService.getWaitUser()) || room.equals(gameStateService.getWaitRoom()) ){
            gameStateService.setWaitRoom("");
            gameStateService.clearWaitUser();
            webSocketConfig.broadcast("【全体】匹配中的玩家已经退出了！");
        }
        // 退出房间

    }


}

