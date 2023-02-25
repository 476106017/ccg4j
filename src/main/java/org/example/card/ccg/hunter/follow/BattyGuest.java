package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class BattyGuest extends FollowCard {
    private String name = "狂蝠来宾";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "猎人";
    private List<String> race = Lists.ofStr();
    private String mark = """
        亡语：召唤1只2/1的蝙蝠
        """;
    private String subMark = "";

    public BattyGuest() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            ownerPlayer().summon(createCard(ThirstyBat.class));
        })));
    }


    @Getter
    @Setter
    public static class ThirstyBat extends FollowCard {
        private String name = "饥渴的蝙蝠";
        private Integer cost = 1;
        private int atk = 2;
        private int hp = 1;
        private String job = "猎人";
        private List<String> race = Lists.ofStr("野兽");
        private String mark = """
        """;
        private String subMark = "";

        public ThirstyBat() {
            setMaxHp(getHp());
        }
    }
}