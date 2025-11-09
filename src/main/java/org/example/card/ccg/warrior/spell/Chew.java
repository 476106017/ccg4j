package org.example.card.ccg.warrior.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Chew extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 1;
    public String name = "咀嚼";
    public String job = "战士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        破坏场上一名随从
        直至下回合结束前，我方主战者获得【受伤时：由其所有者召还该随从】
        """;

    public String subMark = "";

    private FollowCard followCard = null;


    public void init() {
        setPlay(new Play(()->info.getAreaFollowsAsGameObj(),
            true,
            target->{
                followCard = (FollowCard) target;
                destroy(followCard);
                ownerLeader().addEffect(new Effect(this, ownerLeader(), EffectTiming.AfterLeaderDamaged,3,
                    obj-> followCard!=null,
                    obj->{
                        followCard.ownerPlayer().recall(followCard);
                    }),false);
            }
        ));
    }

}
