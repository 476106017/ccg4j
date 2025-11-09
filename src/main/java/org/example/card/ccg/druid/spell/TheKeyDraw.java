package org.example.card.ccg.druid.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class TheKeyDraw extends SpellCard {

   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 3;
    public String name = "关键一抽";
    public String job = "德鲁伊";
    private List<String> race = Lists.ofStr();
    public String mark = """
        揭示：对战开始时
        抽1张费用为5的卡牌
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()-> ownerPlayer().draw(p->p.getCost()==5)));
        addEffects(new Effect(this,this, EffectTiming.InvocationBegin,
            ()->true,()->{}));
    }

}
