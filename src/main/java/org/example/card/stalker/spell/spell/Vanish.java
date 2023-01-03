package org.example.card.stalker.spell.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class Vanish extends SpellCard {
    public Integer cost = 6;
    public String name = "消失";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        返回场上全部随从
        """;

    public String subMark = "";

    public Vanish() {
        getPlays().add(new Event.Play(
            () -> {
                // TODO 要同时触发离场时
            }
        ));
    }
}
