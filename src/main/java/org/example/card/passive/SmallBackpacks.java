package org.example.card.passive;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class SmallBackpacks extends SpellCard {
    public Integer cost = 3;
    public String name = "小型背包";
    public String job = "被动";
    private List<String> race = Lists.ofStr();
    public String mark = """
        抽2张牌
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            ownerPlayer().draw(2);
        }));
    }

}
