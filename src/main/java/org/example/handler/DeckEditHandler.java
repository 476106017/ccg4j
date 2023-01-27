package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.card.Card;
import org.example.card.shadowverse.neutral.SVPlayer;
import org.example.constant.DeckPreset;
import org.example.game.Leader;
import org.example.game.PlayerDeck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static org.example.system.Database.userDecks;

@Service
@ConditionalOnClass(SocketIOServer.class)
@Slf4j
public class DeckEditHandler {
    @Autowired
    SocketIOServer socketIOServer;

    @Autowired
    Gson gson;

    @OnEvent(value = "deck") // 选择预设牌组
    public void deck(SocketIOClient client, String data) {
        UUID me = client.getSessionId();
        PlayerDeck myDeck = userDecks.get(me);

        client.sendEvent("receiveMsg",myDeck.describe());
    }

    @OnEvent(value = "usedeck") // 选择预设牌组
    public void usedeck(SocketIOClient client, String data) {
        UUID me = client.getSessionId();

        if(Strings.isBlank(data)){
            client.sendEvent("receiveMsg", DeckPreset.describe());
            return;
        }
        List<Class<? extends Card>> deck = DeckPreset.decks.get(data);
        Class<? extends Leader> leader = DeckPreset.deckLeader.get(data);
        if(deck==null){
            client.sendEvent("receiveMsg", "不存在的牌组名字");
            return;
        }
        if(leader==null){
            leader = SVPlayer.class;
        }

        PlayerDeck playerDeck = userDecks.get(me);
        if(playerDeck==null){
            client.sendEvent("receiveMsg", "不存在的用户，请刷新页面重新登录");
            return;
        }

        List<Class<? extends Card>> activeDeck = playerDeck.getActiveDeck();
        activeDeck.clear();
        activeDeck.addAll(deck);
        playerDeck.setLeaderClass(leader);

        client.sendEvent("receiveMsg", "成功使用预设牌组：" + data);
    }

}
