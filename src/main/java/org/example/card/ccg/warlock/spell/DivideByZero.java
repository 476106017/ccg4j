package org.example.card.ccg.warlock.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class DivideByZero extends SpellCard {
    public Integer cost = 0;
    public String name = "除零错误";
    public String job = "术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        摧毁所有随从...以及你的牌库
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            destroy(info.getAreaFollowsCopy());
            info.exile(ownerPlayer().getDeckCopy());
        }));
    }

}
