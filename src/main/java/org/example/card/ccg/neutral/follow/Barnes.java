package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.game.PlayerInfo;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.ALL_COST;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Barnes extends FollowCard {

   private CardRarity rarity = CardRarity.RAINBOW;
    public Integer cost = 5;
    public String name = "巴内斯";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        战吼：随机挑选你牌库里的一个随从，召唤一个1/1的复制。
        """;
    public String subMark = "";

    public int atk = 3;
    public int hp = 4;

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(() -> {
            final List<Card> deckBy = ownerPlayer().getDeckBy(p -> p instanceof FollowCard);
            final Card follow = Lists.randOf(deckBy);
            if(follow != null) {
                final FollowCard copy = (FollowCard) follow.copyBy(ownerPlayer());
                copy.setAtk(1);
                copy.setHp(1);
                copy.setMaxHp(1);
                ownerPlayer().summon(copy);
            }
        }));
    }
}
