package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class StygianDemise extends FollowCard {
    private String name = "冥河的流葬";
    private Integer cost = 6;
    private int atk = 4;
    private int hp = 5;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：葬送牌堆中消费最高的随从，亡灵召还：10，但在回合结束时死亡
        """;
    private String subMark = "";

    public StygianDemise() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            List<Card> deckBy = ownerPlayer().getDeckBy(card -> card instanceof FollowCard followCard
                && followCard.getCost().equals(
                ownerPlayer().getDeck().stream().filter(deckCard -> deckCard instanceof FollowCard)
                    .map(Card::getCost).max(Comparator.naturalOrder()).orElse(-1))
            );
            if(deckBy.size()>0){
                ownerPlayer().recall(10,followCard -> followCard.addEffects(
                    (new Effect(this,this, EffectTiming.EndTurn, this::death))));
            }
        }));
    }
}