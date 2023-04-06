package org.example.card.ccg.mage.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class MagicTrick extends SpellCard {
    public Integer cost = 1;
    public String name = "魔术戏法";
    public String job = "法师";
    private List<String> race = Lists.ofStr();
    public String mark = """
        发现1张费用不大于2的法术卡
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            ownerPlayer().discoverCard(card -> card instanceof MagicTrick,
                prototype-> ownerPlayer().addHand(prototype.copyBy(ownerPlayer())));
        }));
    }

}
