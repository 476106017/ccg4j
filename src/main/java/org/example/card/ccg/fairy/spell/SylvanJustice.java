package org.example.card.ccg.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.card._derivant.Derivant;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class SylvanJustice extends SpellCard {
    public Integer cost = 2;
    public String name = "森林的反扑";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        对敌方场上一名随从造成3点伤害，增加1张妖精到手牌
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(
            ()->enemyPlayer().getAreaFollowsAsGameObj(), true,
            target->{
                info.damageEffect(this,target,3);
                ownerPlayer().addHand(createCard(Derivant.Fairy.class));
            }));
    }
}
