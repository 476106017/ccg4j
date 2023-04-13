package org.example.card.passive;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class KhadgarsScryingOrb extends SpellCard {
    public Integer cost = 0;
    public String name = "卡德加的占卜宝珠";
    public String job = "被动";
    private List<String> race = Lists.ofStr();
    public String mark = """
        你的法术的法力值消耗减少1点。
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            final List<Card> follows = ownerPlayer().getHandBy(p -> p instanceof SpellCard);
            follows.forEach(card -> {
                card.addCost(-1);
            });
            ownerLeader().addEffect(new Effect(this,ownerLeader(), EffectTiming.WhenAddHand,
                obj->{
                    List<Card> list = (List<Card>) obj;
                    return list.stream().anyMatch(card -> card instanceof SpellCard);
                },
                obj->{
                    List<Card> list = (List<Card>) obj;
                    list.stream().filter(card -> card instanceof SpellCard).forEach(card -> {
                        card.addCost(-1);
                    });
                }
            ));
        }));
    }

}
