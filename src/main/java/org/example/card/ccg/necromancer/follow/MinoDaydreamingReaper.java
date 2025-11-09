package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class MinoDaydreamingReaper extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "妄想死神·米诺";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：如果本场对战中发动亡语的卡牌数大于5，则获得+4/+4、突进
        亡语：墓地+1
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            if(ownerPlayer().getCount(EffectTiming.DeathRattle.getName()) >= 5){
                addStatus(4,4);
                addKeyword("突进");
            }
        }));
        addEffects((new Effect(this,this,
            EffectTiming.DeathRattle, obj -> {
                ownerPlayer().countToGraveyard(1);
        })));
    }
}
