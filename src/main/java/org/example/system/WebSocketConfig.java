package org.example.system;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.util.Strings;
import org.example.card.Card;
import org.example.card.ccg.neutral.ThePlayer;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.example.game.PlayerInfo;
import org.example.handler.ChatHandler;
import org.example.handler.DeckEditHandler;
import org.example.handler.GameHandler;
import org.example.handler.MatchHandler;
import org.example.system.util.SpringContext;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;

import static org.example.system.Database.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig  {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @ServerEndpoint(value = "/socket/{name}",encoders = {GsonConfig.MyEncoder.class})
    @Service
    @DependsOn({"chatHandler","deckEditHandler","gameHandler","matchHandler"})
    public static class Handler{
        ChatHandler chatHandler = SpringContext.getBean(ChatHandler.class);

        DeckEditHandler deckEditHandler = SpringContext.getBean(DeckEditHandler.class);

        GameHandler gameHandler = SpringContext.getBean(GameHandler.class);

        MatchHandler matchHandler = SpringContext.getBean(MatchHandler.class);

        @OnOpen
        public void onOpen(Session session) throws IOException {
            // handle open event
            final String name = session.getPathParameters().get("name");
            if(Strings.isBlank(name) || userNames.containsValue(name)){
                session.getBasicRemote().sendText("用户名无法使用！");
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
            session.getBasicRemote().sendText(name + "登录成功！");
            final int size = userNames.size();
            broadcast("【全体】有玩家登陆了游戏！当前在线："+ size +"人");
        }

        @OnClose
        public void onClose(Session session) {
            // handle close event
            String name = userNames.get(session);
            userNames.remove(session);
            final int size = userNames.size();
            broadcast("【全体】有玩家退出了游戏！当前在线："+ size +"人");
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
                broadcast("【全体】匹配中的玩家已经退出了！");
            }
        }

        @OnMessage
        public void onMessage(String msg, Session session) {
            // handle message event
            final String[] split = msg.split("::");
            try {

                switch (split[0]){
                    case "joinRoom" -> matchHandler.joinRoom(session);
                    case "leave" -> matchHandler.leave(session);

                    case "deck" -> deckEditHandler.deck(session);
                    case "usedeck" -> deckEditHandler.usedeck(session,split[1]);
                    case "roomPresetChat" -> chatHandler.roomPresetChat(session,split[1]);

                    case "swap" -> gameHandler.swap(session,split[1]);
                    case "play" -> gameHandler.play(session,split[1]);
                    case "attack" -> gameHandler.attack(session,split[1]);
                    case "discover" -> gameHandler.discover(session,split[1]);
                    case "skill" -> gameHandler.skill(session,split[1]);

                    case "turnEnd" -> gameHandler.turnEnd(session,split[1]);
                    case "test" -> gameHandler.test(session);
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

    public static void broadcast(String msg){
        userNames.keySet().forEach(userSession->{
            try {
                userSession.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
