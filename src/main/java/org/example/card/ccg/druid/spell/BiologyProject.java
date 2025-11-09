package org.example.card.ccg.druid.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class BiologyProject extends SpellCard {

   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 1;
    public String name = "生物计划";
    public String job = "德鲁伊";
    private List<String> race = Lists.ofStr("自然");
    public String mark = """
        每个玩家获得两个法力水晶。
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()->{
            ownerPlayer().addPpMax(2);
            ownerPlayer().addPp(2);
            enemyPlayer().addPpMax(2);
            enemyPlayer().addPp(2);
        }));
    }

}
