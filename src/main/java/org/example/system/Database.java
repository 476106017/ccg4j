package org.example.system;

import com.google.gson.Gson;
import jakarta.websocket.Session;
import org.example.card.Card;
import org.example.card.data.CardDataLoader;
import org.example.game.GameInfo;
import org.example.game.PlayerDeck;
import org.example.system.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Predicate;

@Component
public class Database {

    public static Gson gson;

    @Autowired
    public void setGson(Gson gson) {
        Database.gson = gson;
    }

    public static Map<Session,String> userNames = new ConcurrentHashMap<>();
    public static Map<Session, Long> sessionUserIds = new ConcurrentHashMap<>();
    public static Map<Session, PlayerDeck> userDecks = new ConcurrentHashMap<>();
    public static Map<Session, String > userRoom = new ConcurrentHashMap<>();
    public static String waitRoom = "";// 匹配中的房间
    public static Session waitUser;// 匹配中的人
    public static Map<String, GameInfo> roomGame = new ConcurrentHashMap<>();

    public static  Map<String,ScheduledExecutorService> roomSchedule = new ConcurrentHashMap<>();

    public static Map<String,Class<? extends Card>> nameToCardClass = new ConcurrentHashMap<>();
    public static Map<Class<? extends Card>, Card> prototypes = new ConcurrentHashMap<>();
    
    // 单例模式访问卡牌初始属性（支持Java类和数据驱动）
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
    
    /**
     * 根据卡牌名称获取原型（支持Java类和数据驱动）
     */
    public static Card getPrototypeByName(String cardName) {
        // 1. 处理 data: 前缀（来自 CardCatalogService）
        if (cardName.startsWith("data:")) {
            String realName = cardName.substring(5);
            Card dataCard = CardDataLoader.getPrototypeByName(realName);
            if (dataCard != null) {
                return dataCard;
            }
        }

        // 2. 优先从数据驱动卡牌中查找（直接使用名称）
        Card dataCard = CardDataLoader.getPrototypeByName(cardName);
        if (dataCard != null) {
            return dataCard;
        }
        
        // 3. 从Java类卡牌中查找（通过名称映射）
        Class<? extends Card> cardClass = nameToCardClass.get(cardName);
        if (cardClass != null) {
            return getPrototype(cardClass);
        }

        // 4. 尝试作为类名加载（来自 CardCatalogService 的 Java 卡牌 code）
        try {
            Class<?> clazz = Class.forName(cardName);
            if (Card.class.isAssignableFrom(clazz)) {
                return getPrototype((Class<? extends Card>) clazz);
            }
        } catch (ClassNotFoundException | ClassCastException e) {
            // 忽略，说明不是类名
        }
        
        return null;
    }
    
    public static Card getPrototypeBy(Predicate<Card> predicate){
        List<Card> prototypeBy = getPrototypeBy(predicate, 1);
        return prototypeBy.isEmpty()?null:prototypeBy.get(0);
    }
    
    public static List<Card> getPrototypeBy(Predicate<Card> predicate, int num){
        // 合并Java类卡牌和数据驱动卡牌
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(prototypes.values());
        allCards.addAll(CardDataLoader.getAllDataCardPrototypes().values());
        
        final List<Card> list = allCards.stream().filter(predicate).toList();
        return Lists.randOf(list,num).stream().map(Card::prototype).toList();
    }
}
