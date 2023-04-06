package org.example.card.ccg.vampire.amulet;

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
public class RestlessParish extends AmuletCard {

    public Integer cost = 0;

    public String name = "诡谲的幽暗街道";
    public String job = "吸血鬼";
    private List<String> race = Lists.ofStr();

    public String mark = """
        战吼：给予自己的主战者1点伤害。
        亡语：抽取1张卡片。
        """;
    public String subMark = "";

    List<FollowCard> effectFollows = new ArrayList<>();

    public void init() {
        setCountDown(1);
        setPlay(new Play(()->info.damageEffect(this,ownerLeader(),1)));
        addEffects((new Effect(this,this,
            EffectTiming.DeathRattle, obj -> ownerPlayer().draw(1))));
    }

}
