package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class Peddlefeet extends FollowCard {
    public Integer cost = 4;
    public String name = "匹德菲特";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        我方出牌时：如果使用的是随从，则搜索全部同名牌
        """;
    public String subMark = "";


    public int atk = 3;
    public int hp = 3;

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.WhenPlay,
            obj-> obj instanceof FollowCard,
            obj->{
                FollowCard follow = (FollowCard)obj;
                ownerPlayer().draw(
                    card -> card.getName().equals(follow.getName()),
                    ownerPlayer().getDeckMax());
            })));
    }
}
