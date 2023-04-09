package org.example.card.ccg.sts.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Angry extends SpellCard {
    public Integer cost = 0;
    public String name = "愤怒";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        对1名敌方随从造成6(8)点伤害
        将1张愤怒洗入牌堆
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->enemyPlayer().getAreaFollowsAsGameObj(),
            true,
            target->{
                info.damageEffect(this,target,isUpgrade()?8:6);
                ownerPlayer().addDeck(createCard(Angry.class));
            }
        ));
    }

}
