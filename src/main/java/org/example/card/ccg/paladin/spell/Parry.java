package org.example.card.ccg.paladin.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Parry extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "招架";
    public String job = "圣骑士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        我方主战者获得【圣盾】
        """;

    public String subMark = "";


    public void init() {
        getKeywords().add("速攻");
        setPlay(new Play(()-> ownerPlayer().count("圣盾")));
    }

}
