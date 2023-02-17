package org.example.card.ccg.necromancer.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card._derivant.Derivant;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class CorralSouls extends SpellCard {
    public Integer cost = 2;
    public String name = "统率灵魂";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        破坏1个自己的随从
        召唤2个怨灵
        抽2张牌
        """;

    public String subMark = "";


    public CorralSouls() {
        setPlay(new Play(()->ownerPlayer().getAreaFollowsAsGameObj(),
            true,
            obj->{
                destroy((FollowCard)obj);
                ownerPlayer().summon(List.of(createCard(Derivant.Ghost.class),createCard(Derivant.Ghost.class)));
                ownerPlayer().draw(2);
            }));
    }

}
