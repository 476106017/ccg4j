package org.example.card.ccg.sts.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.POISON;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Envenom extends AmuletCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "涂毒";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        每有一次攻击对主战者造成未被格挡的伤害，就给予1层中毒。
        """;

    public String subMark = "";

    public void init() {
        addEffects(new Effect(this,this, EffectTiming.BeginTurn,
            ()->{
                final int n = isUpgrade()?3:2;
                enemyPlayer().count(POISON,n);
            }));
    }
}
