package org.example.system;

import org.example.card.Card;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

public class Database {

    public static Map<UUID,String> userNames = new ConcurrentHashMap<>();
    public static Map<UUID, PlayerDeck> userDecks = new ConcurrentHashMap<>();
    public static Map<UUID, String > userRoom = new ConcurrentHashMap<>();
    public static String waitRoom = "";// 匹配中的房间
    public static UUID waitUser;// 匹配中的人
    public static Map<String,  UUID> roomReadyMatch = new ConcurrentHashMap<>();
    public static Map<String, GameInfo> roomGame = new ConcurrentHashMap<>();

    public static  Map<String,ScheduledExecutorService> roomSchedule = new ConcurrentHashMap<>();

    public static Map<Class<? extends Card>, Card> prototypes = new ConcurrentHashMap<>();
    // 单例模式访问卡牌初始属性
    public static <T extends Card> T getPrototype(Class<T> clazz) {
        Card prototype = prototypes.get(clazz);
        if(prototype!=null) return (T)prototype;
        try {
            Card card = clazz.getDeclaredConstructor().newInstance();
            prototypes.put(clazz,card);
            return (T)card;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
    public static Card getPrototypeBy(Predicate<Card> predicate){
        List<Card> prototypeBy = getPrototypeBy(predicate, 1);
        return prototypeBy.isEmpty()?null:prototypeBy.get(0);
    }
    public static List<Card> getPrototypeBy(Predicate<Card> predicate, int num){
        return prototypes.values().stream().filter(predicate).limit(num).map(Card::prototype).toList();
    }
}
