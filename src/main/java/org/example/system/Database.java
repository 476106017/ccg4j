package org.example.system;

import org.example.card.Card;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;

public class Database {

    public static Map<UUID,String> userNames = new ConcurrentHashMap<>();
    public static Map<UUID, PlayerDeck> userDecks = new ConcurrentHashMap<>();
    public static String waitRoom = "";// 匹配中的房间
    public static UUID waitUser;// 匹配中的人
    public static Map<String,  UUID> roomReadyMatch = new ConcurrentHashMap<>();
    public static Map<String, GameInfo> roomGame = new ConcurrentHashMap<>();

    public static  Map<String,ScheduledExecutorService> roomSchedule = new ConcurrentHashMap<>();

    public static Map<Class<? extends Card>, Card> prototypes = new ConcurrentHashMap<>();

}
