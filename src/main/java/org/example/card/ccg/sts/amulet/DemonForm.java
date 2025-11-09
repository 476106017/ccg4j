package org.example.card.ccg.sts.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.STRENGTH;
import org.example.constant.CardRarity;

@Getter
@Setter
public class DemonForm extends AmuletCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 4;
    public String name = "恶魔形态";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        回合开始时：获得2(3)点力量
        """;

    public String subMark = "";

    public void init() {
        addEffects(new Effect(this,this, EffectTiming.BeginTurn,
            ()->ownerPlayer().count(STRENGTH,isUpgrade()?3:2)));
    }
}
