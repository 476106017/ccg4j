package org.example.card.other.test.folow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class TestFollow extends FollowCard {
    private String name = "测试随从";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 2;
    private String job = "测试";
    private List<String> race = Lists.ofStr();
    private String mark = """
        """;
    private String subMark = "";


    public TestFollow() {
        setMaxHp(getHp());
//        getKeywords().add("剧毒");
        setPlay(new Play(() -> {
            getInfo().msg(hashCode() + "战吼");
            ownerPlayer().summon((TestFollow)cloneOfMe());
        }));
        addEffects((new Effect(this,this, EffectTiming.Entering, obj->
            getInfo().msg(hashCode() + "入场时"))));

    }
}