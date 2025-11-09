package org.example.card.ccg.mage.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.BLOCK;
import org.example.constant.CardRarity;

@Getter
@Setter
public class IceBarrier extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "寒冰护体";
    public String job = "法师";
    private List<String> race = Lists.ofStr("冰霜");
    public String mark = """
        获得8层格挡
        """;

    public String subMark = "";


    public void init() {
        getKeywords().add("速攻");
        setPlay(new Play(()->{
            ownerPlayer().count(BLOCK,8);
        }));
    }
}
