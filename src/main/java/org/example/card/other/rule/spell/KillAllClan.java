package org.example.card.other.rule.spell;

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
public class KillAllClan extends SpellCard {
    public Integer cost = 9;
    public String name = "诛灭九族";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr();
    public String mark = """
        破坏敌方场上全部随从，
        如果创造这些随从的是其他卡片、且不在墓地或是被除外，则破坏其创造者，重复9次。
        """;

    public String subMark = "";
    public int target = 2;

    public KillAllClan() {// TODO 施工中
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
