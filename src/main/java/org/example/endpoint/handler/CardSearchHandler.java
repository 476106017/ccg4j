package org.example.endpoint.handler;

import com.google.gson.Gson;
import jakarta.websocket.Session;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import org.example.card.Card;
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

        Collection<Class<? extends Card>> allCards = Database.nameToCardClass.values();
        List<Card> searchResults = allCards.stream()
            .map(cardClass -> {
                try {
                    Card card = cardClass.getDeclaredConstructor().newInstance();
                    card.init();  // 初始化卡牌
                    return card;
                } catch (Exception e) {
                    return null;
                }
            })
            .filter(card -> card != null)
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
