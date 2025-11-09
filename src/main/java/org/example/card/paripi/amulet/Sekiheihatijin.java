package org.example.card.paripi.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Sekiheihatijin extends AmuletCard {


   private CardRarity rarity = CardRarity.GOLD;
    public Integer cost = 8;

    public String name = "石兵八阵";
    public String job = "派对咖";
    private List<String> race = Lists.ofStr("阵法");

    public String mark = """
        双方随从攻击时，己方场上每有1个随从便降低15%命中率
        """;
    public String subMark = "";

    public void init() {

        addEffects((new Effect(this,this, EffectTiming.WhenOtherAttack, obj->{
            Damage damage = (Damage) obj;
            int size = damage.getFrom().ownerPlayer().getAreaFollows().size();

            if(Math.random()> 0.15*size)return;
            getInfo().msg("丢失！");
            damage.setMiss(true);
        }
        )));
    }

}
