package org.example.handler;

import com.google.gson.Gson;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.example.card.Card;
import org.example.card.ccg.neutral.ThePlayer;
import org.example.constant.DeckPreset;
import org.example.game.Leader;
import org.example.game.PlayerDeck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.example.system.Database.userDecks;

@Service
@Slf4j
public class DeckEditHandler {
    @Autowired
    Gson gson;


    public void deck(Session client) throws IOException, EncodeException {
        PlayerDeck myDeck = userDecks.get(client);

//        client.getBasicRemote().sendObject(myDeck.describeJson());
        client.getBasicRemote().sendText(myDeck.describe());
    }


    public void usedeck(Session client, String data) throws IOException, EncodeException {

        final Map<String, List<Class<? extends Card>>> decks = DeckPreset.decks;
        if(Strings.isBlank(data)){
            // region 如果不输入选择牌组，则返回全部牌组
            client.getBasicRemote().sendText(DeckPreset.describe());
//            client.getBasicRemote().sendObject(DeckPreset.describeJson());
            return;
            // endregion 如果不输入选择牌组，则返回全部牌组
        }
        List<Class<? extends Card>> deck = decks.get(data);
        Class<? extends Leader> leader = DeckPreset.deckLeader.get(data);
        if(deck==null){
            client.getBasicRemote().sendText( "不存在的牌组名字");
            return;
        }
        if(leader==null){
            leader = ThePlayer.class;
        }

        PlayerDeck playerDeck = userDecks.get(client);
        if(playerDeck==null){
            client.getBasicRemote().sendText( "不存在的用户，请刷新页面重新登录");
            return;
        }

        List<Class<? extends Card>> activeDeck = playerDeck.getActiveDeck();
        activeDeck.clear();
        activeDeck.addAll(deck);
        playerDeck.setLeaderClass(leader);

        client.getBasicRemote().sendText( "成功使用预设牌组：" + data);
    }

}
