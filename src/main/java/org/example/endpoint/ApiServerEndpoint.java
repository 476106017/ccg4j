package org.example.endpoint;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.util.Strings;
import org.example.card.Card;
import org.example.card.ccg.neutral.ThePlayer;
import org.example.endpoint.handler.ChatHandler;
import org.example.endpoint.handler.DeckEditHandler;
import org.example.endpoint.handler.GameHandler;
import org.example.endpoint.handler.MatchHandler;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.example.game.PlayerInfo;
import org.example.system.GsonConfig;
import org.example.system.WebSocketConfig;
import org.example.system.WebSocketConfigurator;
import org.example.system.util.Msg;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.example.system.Database.*;

@ServerEndpoint(value = "/api/{name}",
    encoders = {GsonConfig.MyEncoder.class},
    configurator = WebSocketConfigurator.CustomSpringConfigurator.class)
@Service
@DependsOn({"chatHandler","deckEditHandler","gameHandler","matchHandler"})
public class ApiServerEndpoint {
    @Autowired ChatHandler chatHandler;
    @Autowired DeckEditHandler deckEditHandler;
    @Autowired GameHandler gameHandler;
    @Autowired MatchHandler matchHandler;

    @OnOpen
    public void onOpen(Session session) throws IOException {
        // handle open event
        final String name = session.getPathParameters().get("name");
        if(Strings.isBlank(name) || userNames.containsValue(name)){
            Msg.send(session,"用户名无法使用！");
            session.close();
            return;
        }
        session.getUserProperties().put("name",name);
        userNames.put(session,name);

        // region
        PlayerDeck playerDeck = new PlayerDeck();
        playerDeck.setLeaderClass(ThePlayer.class);

        Set<Class<? extends Card>> subTypesOf =
            new Reflections(new ConfigurationBuilder()
                .filterInputsBy(s -> !s.contains("genshin"))
                .forPackage("org.example.card"))
                .getSubTypesOf(Card.class);
        // 移除不符合的卡牌类型
        subTypesOf.removeIf(aClass ->{
            int modifiers = aClass.getModifiers();
            return Modifier.isAbstract(modifiers) || Modifier.isStatic(modifiers);
        });
        // 随机取30张
        List<Class<? extends Card>> classes = new ArrayList<>(subTypesOf.stream().toList());
        Collections.shuffle(classes);
        playerDeck.getActiveDeck().addAll(classes.subList(0,30));
        userDecks.put(session, playerDeck);
        // endregion
Msg.send(session,name + "登录成功！");
        final int size = userNames.size();
        WebSocketConfig.broadcast("【全体】有玩家登陆了游戏！当前在线："+ size +"人");
    }

    @OnClose
    public void onClose(Session session) {
        // handle close event
        String name = userNames.get(session);
        userNames.remove(session);
        final int size = userNames.size();
        WebSocketConfig.broadcast("【全体】有玩家退出了游戏！当前在线："+ size +"人");
        String room = userRoom.get(session);
        if(room==null)return;

        GameInfo info = roomGame.get(room);
        if(info!=null){
            PlayerInfo player = info.playerBySession(session);
            PlayerInfo enemy = info.anotherPlayerBySession(session);
            info.msg(player.getName() + "已断开连接！");
            info.gameset(enemy);
            return;
        }
        // 释放资源
        roomGame.remove(room);
        userRoom.remove(session);
        if(session==waitUser || room.equals(waitRoom) ){
            waitRoom = "";
            waitUser = null;
            WebSocketConfig.broadcast("【全体】匹配中的玩家已经退出了！");
        }
    }

    @OnMessage
    public void onMessage(String msg, Session session) {
        // handle message event
        final String[] split = msg.trim().split("::");
        try {

            String param;
            if(split.length<2 || Strings.isBlank(split[1]))
                param = "";
            else param = split[1];

            switch (split[0]){
                case "joinRoom" -> matchHandler.joinRoom(session);
                case "leave" -> matchHandler.leave(session);

                case "deck" -> deckEditHandler.deck(session);
                case "usedeck" -> deckEditHandler.usedeck(session, param);
                case "chat" -> chatHandler.chat(session, param);

                case "swap" -> gameHandler.swap(session, param);
                case "play" -> gameHandler.play(session, param);
                case "attack" -> gameHandler.attack(session, param);
                case "discover" -> gameHandler.discover(session, param);
                case "skill" -> gameHandler.skill(session, param);

                case "end" -> gameHandler.end(session, param);
                case "test" -> gameHandler.test(session);
                default -> Msg.send(session,"不存在的指令！");
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @OnError
    public void onError(Throwable t) {
        // handle error event
    }


}