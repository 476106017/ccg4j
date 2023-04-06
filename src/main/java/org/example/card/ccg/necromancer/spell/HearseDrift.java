package org.example.card.ccg.necromancer.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import java.util.Optional;

@Getter
@Setter
public class HearseDrift extends SpellCard {
    public Integer cost = 4;
    public String name = "灵车漂移";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        死灵术 4：召还1名随从，重复此效果直到满场。
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            while (ownerPlayer().getGraveyardCount()>=4 &&
                ownerPlayer().getArea().size() < ownerPlayer().getAreaMax())
            {
                Optional<Card> first = ownerPlayer().getGraveyard().stream().filter(card -> card instanceof FollowCard)
                    .findFirst();
                if(first.isPresent()){
                    ownerPlayer().recall((FollowCard)first.get());
                    ownerPlayer().countToGraveyard(-4);
                }else break;
            }
        }));
    }

}
