package org.example.card.ccg.neutral.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class Mythcleaver extends AmuletCard {

    public Integer cost = 6;

    public String name = "神话战剑";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public transient int countDown = 3;

    public String mark = """
        战吼：除外敌方的1个护符
        回合结束时：随机除外敌方1个随从
        """;
    public String subMark = "";

    List<FollowCard> effectFollows = new ArrayList<>();

    public Mythcleaver() {
        setPlay(new Play(()->enemyPlayer().getAreaAsGameObjBy(card -> card instanceof AmuletCard),
            false,
            obj-> info.exile((AmuletCard) obj)));
        addEffects((new Effect(this,this,
            EffectTiming.EndTurn, obj -> info.exile(enemyPlayer().getAreaRandomFollow()))));
    }

}
