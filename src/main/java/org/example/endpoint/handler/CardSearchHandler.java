package org.example.endpoint.handler;

import com.google.gson.Gson;
import jakarta.websocket.Session;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.example.card.Card;
import org.example.card.data.CardDataLoader;
import org.example.system.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.example.system.util.Msg;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CardSearchHandler {

    @Autowired
    private Gson gson;

    // 移除不必要的 DTO 类，直接使用参数

    public void searchCards(Session client, Map<String, Object> data) throws IOException {
        String name = data != null && data.get("name") != null ? ((String) data.get("name")).toLowerCase() : "";
        String cardType = data != null && data.get("cardType") != null ? (String) data.get("cardType") : "";
        String cost = data != null && data.get("cost") != null ? (String) data.get("cost") : "";

        // 将搜索词拆分为多个关键词
        String[] searchTerms = name.split("\\s+");

        // 获取Java类卡牌
        Collection<Class<? extends Card>> javaCardClasses = Database.nameToCardClass.values();
        Stream<Card> javaCards = javaCardClasses.stream()
            .map(cardClass -> {
                try {
                    Card card = cardClass.getDeclaredConstructor().newInstance();
                    card.init();
                    return card;
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(card -> card != null);

        // 获取数据驱动卡牌
        Stream<Card> dataCards = CardDataLoader.getAllDataCardPrototypes().values().stream()
            .map(prototype -> {
                Card card = prototype.prototype(); // 创建副本
                card.init(); // 必须初始化才能获取到name等字段
                return card;
            });

        // 合并两种卡牌并进行搜索
        List<Card> searchResults = Stream.concat(javaCards,dataCards)
            .filter(card -> {
                if (!Strings.isBlank(name)) {
                    // 转换卡牌的所有可搜索文本为小写
                    String cardText = (card.getName() + " " +
                                    card.getMark() + " " +
                                    card.getKeywordStr() + " " +
                                    String.join(" ", card.getRace()) + " " +
                                    card.getJob()).toLowerCase();

                    // 所有搜索词都必须匹配
                    for (String term : searchTerms) {
                        if (!cardText.contains(term)) {
                            return false;
                        }
                    }
                }

                if (!Strings.isBlank(cardType) &&
                    !card.getType().equals(cardType)) {
                    return false;
                }

                if (!Strings.isBlank(cost)) {
                    int costFilter = Integer.parseInt(cost);
                    if (costFilter == 7) {
                        if (card.getCost() < 7) {
                            return false;
                        }
                    } else if (card.getCost() != costFilter) {
                        return false;
                    }
                }

                return true;
            })
            .collect(Collectors.toList());

        Msg.send(client, "search_results", searchResults);
    }
}
