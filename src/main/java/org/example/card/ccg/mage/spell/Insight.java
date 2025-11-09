package org.example.card.ccg.mage.spell;

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
public class Insight extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 1;
    public String name = "智慧之光";
    public String job = "法师";
    private List<String> race = Lists.ofStr();
    public String mark = """
        抽1张牌
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            ownerPlayer().draw(1);
        }));
    }
}
