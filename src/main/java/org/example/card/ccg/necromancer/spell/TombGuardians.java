package org.example.card.ccg.necromancer.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.card.ccg.necromancer.follow.Zombie;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TombGuardians extends SpellCard {
    public Integer cost = 4;
    public String name = "坟墓守卫";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        召唤2个具有【守护】的僵尸
        死灵术 4：并获得【复生】
        """;

    public String subMark = "";


    public TombGuardians() {
        setPlay(new Play(()->{
            List<AreaCard> zombies = new ArrayList<>();
            zombies.add(createCard(Zombie.class,"守护"));
            zombies.add(createCard(Zombie.class,"守护"));
            ownerPlayer().summon(zombies);
            ownerPlayer().costGraveyardCountTo(4,()->
                zombies.forEach(zombie-> zombie.addKeyword("复生")));
        }));
    }

}
