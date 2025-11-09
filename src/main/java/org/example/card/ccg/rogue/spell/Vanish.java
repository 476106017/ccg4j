package org.example.card.ccg.rogue.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Vanish extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 6;
    public String name = "消失";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        返回场上全部随从
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(
            () -> {
                ownerPlayer().getAreaCopy().forEach(AreaCard::backToHand);
                enemyPlayer().getAreaCopy().forEach(AreaCard::backToHand);
                info.startEffect();
            }));
    }
}
