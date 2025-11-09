package org.example.card.ccg.necromancer.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class SoulConversion extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 1;
    public String name = "灵魂转移";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        破坏1个自己的随从
        抽2张牌
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->ownerPlayer().getAreaFollowsAsGameObj(),
            true,
            obj->{
                destroy((FollowCard)obj);
                ownerPlayer().draw(2);
            }));
    }

}
