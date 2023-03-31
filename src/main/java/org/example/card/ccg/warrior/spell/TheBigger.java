package org.example.card.ccg.warrior.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class TheBigger extends SpellCard {
    public Integer cost = 2;
    public String name = "比大小";
    public String job = "战士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        抽1张牌，如果你的牌堆张数多于对手牌堆，再抽1张
        """;

    public String subMark = "";


    public TheBigger() {
        setPlay(new Play(()->{
            ownerPlayer().draw(1);
            if(ownerPlayer().getDeck().size()>enemyPlayer().getDeck().size()){
                ownerPlayer().draw(1);
            }
        }));
    }

}
