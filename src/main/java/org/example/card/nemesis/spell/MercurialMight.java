package org.example.card.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class MercurialMight  extends SpellCard {
    public Integer cost = 1;
    public String name = "水银的断绝";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        直到下个回合开始，使主战者获得效果伤害免疫
        """;

    public String subMark = "";

    public MercurialMight() {

        setPlay(new Play(ArrayList::new, false,
            gameObjs -> {
                // 增加主战者效果
                ownerPlayer().getLeader().addEffect(new Effect(
                    this, ownerPlayer().getLeader(),EffectTiming.BeforeDamaged, 2,
                    obj-> {
                        Damage damage = (Damage) obj;
                        if(!damage.isFromAtk()) damage.setDamage(0);}),false);
            }));
    }
}
