package org.example.card.passive;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.paripi.Kongming;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class ScepterOfSummoning extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "召唤权杖";
    public String job = "被动";
    private List<String> race = Lists.ofStr();
    public String mark = """
        牌组中法力值消耗大于或等于5点的随从的法力值消耗变为5点。
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            final List<Card> follows = ownerPlayer().getHandBy(p -> p instanceof FollowCard);
            follows.forEach(card -> {
                if(card.getCost()>5){card.setCost(5);}
            });
            ownerLeader().addEffect(new Effect(this,ownerLeader(), EffectTiming.WhenAddHand,
                obj->{
                    List<Card> list = (List<Card>) obj;
                    return list.stream().anyMatch(card -> card instanceof FollowCard);
                },
                obj->{
                    List<Card> list = (List<Card>) obj;
                    list.stream().filter(card -> card instanceof FollowCard).forEach(card -> {
                        if(card.getCost()>5){card.setCost(5);}
                    });
                }
            ));
        }));
    }

}
