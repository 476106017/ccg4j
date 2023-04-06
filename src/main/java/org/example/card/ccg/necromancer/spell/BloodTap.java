package org.example.card.ccg.necromancer.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class BloodTap extends SpellCard {
    public Integer cost = 2;
    public String name = "活力分流";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        使手牌中的随从获得+1/+1
        死灵术 3：再次获得+1/+1
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
                ownerPlayer().getHandFollows().forEach(followCard ->
                    followCard.addStatus(1,1));

                ownerPlayer().costGraveyardCountTo(3,()->
                    ownerPlayer().getHandFollows().forEach(followCard ->
                        followCard.addStatus(1,1)));
            }));
    }

}
