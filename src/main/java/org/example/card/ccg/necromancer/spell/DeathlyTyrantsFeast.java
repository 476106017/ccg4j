package org.example.card.ccg.necromancer.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class DeathlyTyrantsFeast extends SpellCard {
    public Integer cost = 4;
    public String name = "死之龙的暴食";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        使1个己方随从+3/+3
        死灵术 20：效果变为+10/+10
        """;

    public String subMark = "";


    public DeathlyTyrantsFeast() {
        setPlay(new Play(()->ownerPlayer().getAreaFollowsAsGameObj(),
            true,
            obj->{
                if(!ownerPlayer().costGraveyardCountTo(20,
                    ()->((FollowCard)obj).addStatus(10,10))){
                    ((FollowCard)obj).addStatus(3,3);
                }
            }));
    }

}
