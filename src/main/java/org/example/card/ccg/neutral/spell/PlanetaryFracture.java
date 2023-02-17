package org.example.card.ccg.neutral.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class PlanetaryFracture extends SpellCard {
    public Integer cost = 2;
    public String name = "世界陨灭";
    public String job = "中立";
    private List<String> race = Lists.ofStr("灾厄");
    public String mark = """
        破坏双方场上费用5以上的随从
        如果是第10回合后，则再破坏敌方场上所有随从
        """;

    public String subMark = "当前是第{}回合";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",info.getTurn()+"");
    }

    public PlanetaryFracture() {
        setPlay(new Play(()->{
            List<AreaCard> toDestroy = new ArrayList<>();
            toDestroy.addAll(ownerPlayer().getAreaFollowsBy(followCard -> followCard.getCost()>5));
            toDestroy.addAll(enemyPlayer().getAreaFollowsBy(followCard -> followCard.getCost()>5));
            destroy(toDestroy);

            if(info.getTurn()>=10)
                destroy(enemyPlayer().getArea());
        }));
    }

}
