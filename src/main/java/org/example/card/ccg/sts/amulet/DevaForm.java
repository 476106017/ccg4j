package org.example.card.ccg.sts.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.STRENGTH;
import org.example.constant.CardRarity;

@Getter
@Setter
public class DevaForm extends AmuletCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "天人形态";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        回合开始时：获得一个法力水晶
        """;

    public String subMark = "";

    public void init() {
        setCountDown(3);
        getKeywords().add("虚无");
        addEffects(new Effect(this,this, EffectTiming.BeginTurn,
            ()->{
                ownerPlayer().addPpMax(1);
                ownerPlayer().addPp(1);
            }));
    }
}
