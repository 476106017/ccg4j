package org.example.system;

import jakarta.websocket.Session;
import org.example.card.Card;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.example.system.util.Lists;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

public class Database {

    public static Map<Session,String> userNames = new ConcurrentHashMap<>();
    public static Map<Session, PlayerDeck> userDecks = new ConcurrentHashMap<>();
    public static Map<Session, String > userRoom = new ConcurrentHashMap<>();
    public static String waitRoom = "";// 匹配中的房间
    public static Session waitUser;// 匹配中的人
    public static Map<String, GameInfo> roomGame = new ConcurrentHashMap<>();

    public static  Map<String,ScheduledExecutorService> roomSchedule = new ConcurrentHashMap<>();

    public static Map<String,Class<? extends Card>> nameToCardClass = new ConcurrentHashMap<>();
    public static Map<Class<? extends Card>, Card> prototypes = new ConcurrentHashMap<>();
    // 单例模式访问卡牌初始属性
    public static <T extends Card> T getPrototype(Class<T> clazz) {
        Card prototype = prototypes.get(clazz);
        if(prototype!=null) return (T)prototype;
        try {
            Card card = clazz.getDeclaredConstructor().newInstance();
            card.init();
            prototypes.put(clazz,card);
            nameToCardClass.put(card.getName(),clazz);
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
        final List<Card> list = prototypes.values().stream().filter(predicate).toList();
        return Lists.randOf(list,num).stream().map(Card::prototype).toList();
    }
}
