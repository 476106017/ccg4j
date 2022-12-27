package org.example.card.nemesis.spell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Leader;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class MercurialMight  extends SpellCard {
    public Integer cost = 1;
    public String name = "水银的断绝";
    public String job = "复仇者";
    private List<String> race = List.of();
    public String mark = """
        直到下个回合开始，使主战者获得效果伤害免疫
        """;

    public String subMark = "";

    public MercurialMight() {

        getPlays().add(new Card.Event.Play(ArrayList::new, 0,
            gameObjs -> {// 使用效果
                // 增加主战者效果
                ownerPlayer().getLeader().addEffect(this, EffectTiming.LeaderDamaged, 2,
                    damage-> {if(!damage.isFromAtk()) damage.setDamage(0);});
            }
        ));
    }
}
