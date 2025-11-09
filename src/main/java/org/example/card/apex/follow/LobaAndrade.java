package org.example.card.apex.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.example.constant.CardRarity;


@Getter
@Setter
public class LobaAndrade extends FollowCard {

   private CardRarity rarity = CardRarity.SILVER;
    private String name = "罗芭";
    private Integer cost = 3;
    private int atk = 3;
    private int hp = 4;
    private String job = "APEX";
    private List<String> race = Lists.ofStr("英雄");
    private String mark = """
        战吼：转移到战场左端，并获得一张黑店开张
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            ownerPlayer().getArea().remove(this);
            ownerPlayer().getArea().add(0,this);
            ownerPlayer().addHand(createCard(BlackMarket.class));
        }));
    }

    @Getter
    @Setter
    public static class BlackMarket extends SpellCard {

        private CardRarity rarity = CardRarity.GOLD;
        public Integer cost = 5;
        public String name = "黑店开张";
        public String job = "APEX";
        private List<String> race = Lists.ofStr();
        public String mark = """
        从对方牌库发现一张牌（临时提高发现上限）
        """;

        public String subMark = "";
        public void init() {
            Map<String, List<Card>> index = enemyPlayer().getDeckCopy()
                .stream().collect(Collectors.groupingBy(Card::getName));
            List<Card> cards = enemyPlayer().getDeckCopy().stream()
                .map(Card::getName)
                .distinct()
                .map(name->index.get(name).get(0)).toList();
            setPlay(new Play(()-> {
                final int discoverMax = ownerPlayer().getDiscoverMax();
                ownerPlayer().setDiscoverMax(100);
                ownerPlayer().discoverCard(cards,discoverCard-> {
                    ownerPlayer().setDiscoverMax(discoverMax);
                    discoverCard.removeWhenNotAtArea();
                    discoverCard.changeOwner();
                    ownerPlayer().addHand(discoverCard);
                });
            }));
        }

    }

}
