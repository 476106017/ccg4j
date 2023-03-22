package org.example.card.ccg.festival.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class hsyg extends SpellCard {
    public Integer cost = 5;
    public String name = "黑石摇滚";
    public String job = "战士";
    private List<String> race = Lists.ofStr("火焰");
    public String mark = """
        使牌库中所有随从获得+X/+X（X是它的费用）
        """;

    public String subMark = "";

    public hsyg() {
        setPlay(new Play(
            () -> {
                ownerPlayer().getDeck().forEach(card -> {
                    if(card instanceof FollowCard followCard){
                        followCard.addStatus(followCard.getCost(),followCard.getCost());
                    }
                });
            }));
    }

}
