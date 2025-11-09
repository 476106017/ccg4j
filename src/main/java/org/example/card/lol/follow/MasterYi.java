package org.example.card.lol.follow;

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
public class MasterYi extends FollowCard {

   private CardRarity rarity = CardRarity.SILVER;
    private String name = "无极剑圣";
    private Integer cost = 5;
    private int atk = 4;
    private int hp = 4;
    private String job = "英雄联盟";
    private List<String> race = Lists.ofStr();
    private String mark = """
    击杀时：重置攻击次数
    """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("疾驰");
        addEffects(new Effect(this,this, EffectTiming.WhenKill, obj->{
            setTurnAttack(0);
        }));
    }
}
