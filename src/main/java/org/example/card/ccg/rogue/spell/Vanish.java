package org.example.card.ccg.rogue.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class Vanish extends SpellCard {
    public Integer cost = 1;
    public String name = "讨价还价";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        手牌较少的玩家抽2张牌，另一位玩家增加5点生命上限并回复5点生命
        """;

    public String subMark = "";

    public Vanish() {
        setPlay(new Play(
            () -> {
                int size1 = ownerPlayer().getHand().size();
                int size2 = enemyPlayer().getHand().size();
                if(size1>size2){
                    ownerPlayer().addHpMax(5);
                    ownerPlayer().heal(5);
                    enemyPlayer().draw(2);
                }else {
                    enemyPlayer().addHpMax(5);
                    enemyPlayer().heal(5);
                    ownerPlayer().draw(2);
                }
            }));
    }
}
