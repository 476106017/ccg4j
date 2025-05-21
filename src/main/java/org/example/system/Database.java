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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Database {

    @Autowired
    private CardInitializationService cardInitializationService;

    // 单例模式访问卡牌初始属性
    @SuppressWarnings("unchecked")
    public <T extends Card> T getPrototype(Class<T> clazz) {
        return (T) cardInitializationService.getCardPrototypes().get(clazz);
    }
    public Card getPrototypeBy(Predicate<Card> predicate){
        List<Card> prototypeBy = getPrototypeBy(predicate, 1);
        return prototypeBy.isEmpty()?null:prototypeBy.get(0);
    }
    public List<Card> getPrototypeBy(Predicate<Card> predicate, int num){
        final List<Card> list = cardInitializationService.getCardPrototypes().values().stream().filter(predicate).toList();
        return Lists.randOf(list,num).stream().map(Card::prototype).toList();
    }
}
