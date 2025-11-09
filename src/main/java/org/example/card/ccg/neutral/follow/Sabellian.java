package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Sabellian extends FollowCard {

   private CardRarity rarity = CardRarity.GOLD;
    private String name = "萨贝里安";
    private Integer cost = 9;
    private int atk = 4;
    private int hp = 12;
    private String job = "中立";
    private List<String> race = Lists.ofStr("龙");
    private String mark = """
        若此卡在场上，敌方随从无法触发战吼
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.WhenAtArea, obj->{
            enemyPlayer().setCanFanfare(false);
        })));
        addEffects((new Effect(this,this, EffectTiming.WhenNoLongerAtArea, obj->{
            enemyPlayer().setCanFanfare(true);
        })));

    }
}
