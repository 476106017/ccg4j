package org.example.card.rule.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class Castling extends SpellCard {
    public Integer cost = 1;
    public String name = "王车易位";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr();
    public String mark = """
        交换我方场上2个随从位置
        """;

    public String subMark = "";
    public int target = 2;

    public Castling() {
        setPlay(new Play(()->
            List.of(ownerPlayer().getAreaFollowsAsGameObj(),ownerPlayer().getAreaFollowsAsGameObj()),
            2,true,
            objs->{
                List<AreaCard> area = ownerPlayer().getArea();
                Collections.swap(area,
                    area.indexOf(objs.get(0)),area.indexOf(objs.get(1)));
            }));
    }

}
