package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class MountainBear extends FollowCard {
    private String name = "山岭野熊";
    private Integer cost = 7;
    private int atk = 5;
    private int hp = 6;
    private String job = "猎人";
    private List<String> race = Lists.ofStr("野兽");
    private String mark = """
        亡语：召唤2只山熊宝宝；
        """;

    public String subMark = "";


    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            ownerPlayer().summon(List.of(createCard(MountainCub.class),createCard(MountainCub.class)));
        })));
    }

    @Getter
    @Setter
    public static class MountainCub extends FollowCard {
        private String name = "山熊宝宝";
        private Integer cost = 3;
        private int atk = 2;
        private int hp = 4;
        private String job = "猎人";
        private List<String> race = Lists.ofStr("野兽");
        private String mark = "";
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
            getKeywords().add("守护");
        }
    }
}