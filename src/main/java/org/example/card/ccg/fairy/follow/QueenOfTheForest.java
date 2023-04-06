package org.example.card.ccg.fairy.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.Comparator;
import java.util.List;


@Getter
@Setter
public class QueenOfTheForest extends FollowCard {
    public Integer cost = 3;
    public String name = "森林女王";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("妖精");
    public String mark = """
        战吼：搜索1张费用最低的随从
        亡语：搜索1张费用最高的随从
        """;
    public String subMark = "";

    public int atk = 1;
    public int hp = 2;

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->
            ownerPlayer().draw(card -> card instanceof FollowCard followCard
            && followCard.getCost().equals(
                ownerPlayer().getDeck().stream().filter(deckCard -> deckCard instanceof FollowCard)
                .map(Card::getCost).min(Comparator.naturalOrder()).orElse(-1)))));
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->
            ownerPlayer().draw(card -> card instanceof FollowCard followCard
                && followCard.getCost().equals(
                ownerPlayer().getDeck().stream().filter(deckCard -> deckCard instanceof FollowCard)
                    .map(Card::getCost).max(Comparator.naturalOrder()).orElse(-1))))));
    }

}
