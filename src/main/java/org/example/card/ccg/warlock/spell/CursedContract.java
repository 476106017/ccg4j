package org.example.card.ccg.warlock.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class CursedContract extends SpellCard {
    public Integer cost = 2;
    public String name = "被诅咒的契约";
    public String job = "术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        1. 抽1张牌
        2. 对己方主战者造成3点伤害，抽1张牌
        3. 如果抽到费用更高的牌，重复第2步
        """;

    public String subMark = "";


    public CursedContract() {
        setPlay(new Play(() -> {
            List<Card> draw = ownerPlayer().draw(1);
            Integer cost = draw.get(0).getCost();
            while (true){
                info.damageEffect(this,ownerLeader(),3);

                List<Card> drawNew = ownerPlayer().draw(1);
                Integer costNew = drawNew.get(0).getCost();
                if(costNew <= cost)return;

                cost = costNew;
            }
        }));
    }

}
