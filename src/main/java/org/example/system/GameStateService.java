package org.example.system;

import jakarta.websocket.Session;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

@Service
public class GameStateService {

    private Map<Session, String> userNames = new ConcurrentHashMap<>();
    private Map<Session, PlayerDeck> userDecks = new ConcurrentHashMap<>();
    private Map<Session, String> userRoom = new ConcurrentHashMap<>();
    private String waitRoom = "";
    private Session waitUser = null;
    private Map<String, GameInfo> roomGame = new ConcurrentHashMap<>();
    private Map<String, ScheduledExecutorService> roomSchedule = new ConcurrentHashMap<>();

    public Map<Session, String> getUserNames() {
        return userNames;
    }

    public Map<Session, PlayerDeck> getUserDecks() {
        return userDecks;
    }

    public Map<Session, String> getUserRoom() {
        return userRoom;
    }

    public String getWaitRoom() {
        return waitRoom;
    }

    public void setWaitRoom(String waitRoom) {
        this.waitRoom = waitRoom;
    }

    public Session getWaitUser() {
        return waitUser;
    }

    public void setWaitUser(Session waitUser) {
        this.waitUser = waitUser;
    }

    public void clearWaitUser() {
        this.waitUser = null;
    }

    public Map<String, GameInfo> getRoomGame() {
        return roomGame;
    }

    public Map<String, ScheduledExecutorService> getRoomSchedule() {
        return roomSchedule;
    }
}
