package org.example.card.ccg.shaman.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;

@Getter
@Setter
public class EscapeFromDeath extends SpellCard {
    public Integer cost = 3;
    public String name = "绝处逢生";
    public String job = "萨满";
    private List<String> race = Lists.ofStr();
    public String mark = """
        我方主战者回复与对方回合的使用数等量的hp
        如果该值不小于10，则改为回复hp至上限，并且获得等量的格挡
        """;

    public String subMark = "";


    public void init() {
        getKeywords().add("速攻");
        setPlay(new Play(()->{
            final Integer count = enemyPlayer().getCount(PLAY_NUM);
            if(count<10){
                ownerPlayer().heal(count);
            }else {
                final int heal = ownerPlayer().getHpMax() - ownerPlayer().getHp();
                ownerPlayer().heal(heal);
                ownerPlayer().count("格挡",heal);
            }
        }));
    }

}
