package org.example.card.ccg.shaman.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class AMinorSetback extends SpellCard {
    public Integer cost = 2;
    public String name = "小小挫折";
    public String job = "萨满";
    private List<String> race = Lists.ofStr();
    public String mark = """
        双方PP最大值-1
        """;

    public String subMark = "";


    public AMinorSetback() {
        setPlay(new Play(()->{
                ownerPlayer().setPpMax(ownerPlayer().getPpMax() - 1);
                enemyPlayer().setPpMax(enemyPlayer().getPpMax() - 1);
            }
        ));
    }

}
