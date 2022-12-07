package org.example.system;

import org.example.game.GameInfo;
import org.example.game.PlayerDeck;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Database {

    public static Map<UUID,String> userNames = new ConcurrentHashMap<>();
    public static Map<UUID, PlayerDeck> userDecks = new ConcurrentHashMap<>();
    public static Map<UUID,String> userRoom = new ConcurrentHashMap<>();
    public static Map<String, Set<UUID>> roomUser = new ConcurrentHashMap<>();
    public static String waitRoom = "";// 匹配中的房间
    public static Map<String,  UUID> roomReadyMatch = new ConcurrentHashMap<>();
    public static Map<String, GameInfo> roomGame = new ConcurrentHashMap<>();

}
