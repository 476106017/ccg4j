package org.example.card._derivant;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;

public class Derivant {

    @Getter
    @Setter
    public static class Ghost extends FollowCard {
        private String name = "怨灵";
        private Integer cost = 0;
        private int atk = 1;
        private int hp = 1;
        private String job = "死灵术士";
        private List<String> race = Lists.ofStr();
        private String mark = """
        回合结束时：死亡
        """;
        private String subMark = "";

        public Ghost() {
            setMaxHp(getHp());
            getKeywords().add("疾驰");
            getKeywords().add("游魂");

            addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->
                death()
            )));
        }
    }

    @Getter
    @Setter
    public static class Zombie extends FollowCard {
        private String name = "僵尸";
        private Integer cost = 2;
        private int atk = 2;
        private int hp = 2;
        private String job = "死灵术士";
        private List<String> race = Lists.ofStr();
        private String mark = """
        """;
        private String subMark = "";

        public Zombie() {
            setMaxHp(getHp());
        }
    }

    @Getter
    @Setter
    public static class Skeleton extends FollowCard {
        private String name = "骷髅";
        private Integer cost = 1;
        private int atk = 1;
        private int hp = 1;
        private String job = "死灵术士";
        private List<String> race = Lists.ofStr();
        private String mark = """
        """;
        private String subMark = "";

        public Skeleton() {
            setMaxHp(getHp());
        }
    }
}
