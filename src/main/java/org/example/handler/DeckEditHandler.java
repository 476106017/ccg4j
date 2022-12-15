package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.example.card.Card;
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

    @OnEvent(value = "checkDeck")
    public void checkDeck(SocketIOClient client, String data) {
        UUID sessionId = client.getSessionId();
        PlayerDeck playerDeck = userDecks.get(sessionId);
        socketIOServer.getClient(sessionId).sendEvent("checkDeckResp", playerDeck);
    }

    @OnEvent(value = "editDeck")
    public void editDeck(SocketIOClient client, String data) {
        UUID sessionId = client.getSessionId();
        List<Card> activeDeck = gson.fromJson(data, new TypeToken<List<Card>>() {}.getType());
        userDecks.get(sessionId).setActiveDeck(activeDeck);
        socketIOServer.getClient(sessionId).sendEvent("editDeckResp", "success");
    }

}
