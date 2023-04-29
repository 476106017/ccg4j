package org.example.card.ccg.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class GardenShovel extends SpellCard {
    public Integer cost = 1;
    public String name = "花园铲子";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        铲掉你的一个随从
        """;

    public String subMark = "";

    public void init() {
        getKeywords().add("灵魂绑定");
        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObj(),
            true,
            target->{
                if(target!=null){
                    FollowCard followCard = (FollowCard) target;
                    info.exile(followCard);
                }
            }));
    }
}
