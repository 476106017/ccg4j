package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class BoneFanatic extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "白骨怪客";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        亡语：死灵术 1：召唤1个骷髅士兵
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            ownerPlayer().costMoreGraveyardCountTo(1,
                x -> ownerPlayer().summon(createCard(Derivant.Skeleton.class)));
        })));
    }
}
