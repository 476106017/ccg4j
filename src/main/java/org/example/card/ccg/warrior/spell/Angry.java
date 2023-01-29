package org.example.card.ccg.warrior.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Angry extends SpellCard {
    public Integer cost = 0;
    public String name = "愤怒";
    public String job = "战士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        对1名敌方随从造成6点伤害
        将1张愤怒洗入牌堆
        """;

    public String subMark = "";


    public Angry() {
        setPlay(new Play(()->enemyPlayer().getAreaFollowsAsGameObj(),
            true,
            target->{
                info.damageEffect(this,target,6);
                ownerPlayer().addDeck(createCard(Angry.class));
            }
        ));
    }

}
