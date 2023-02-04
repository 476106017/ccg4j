package org.example.card._derivant;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;

public class Derivant {

    @Getter
    @Setter
    public static class AnalyzingArtifact  extends FollowCard {
        private String name = "解析的造物";
        private Integer cost = 1;
        private int atk = 2;
        private int hp = 1;
        private String job = "复仇者";
        private List<String> race = Lists.ofStr("创造物");
        private String mark = """
        亡语：抽1张牌
        """;
        private String subMark = "";


        public AnalyzingArtifact() {
            setMaxHp(getHp());
            addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
                ownerPlayer().draw(1);
            })));
        }
    }

    @Getter
    @Setter
    public static class Ghost extends FollowCard {
        private String name = "怨灵";
        private Integer cost = 0;
        private int atk = 1;
        private int hp = 1;
        private String job = "死灵术士";
        private List<String> race = Lists.ofStr("灵体");
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
        private List<String> race = Lists.ofStr("不死生物");
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
        private List<String> race = Lists.ofStr("不死生物");
        private String mark = """
        """;
        private String subMark = "";

        public Skeleton() {
            setMaxHp(getHp());
        }
    }

    @Getter
    @Setter
    public static class Fairy extends FollowCard {
        public Integer cost = 1;

        public String name = "妖精";
        public String job = "妖精";
        private List<String> race = Lists.ofStr("妖精");
        public String mark = """
        瞬念召唤：回合结束时剩余1pp（不多不少）
        """;
        public String subMark = "";

        public int atk = 1;
        public int hp = 1;

        public Fairy() {
            setMaxHp(getHp());
            addEffects((new Effect(this,this, EffectTiming.InvocationEnd,
                ()->ownerPlayer().getPpNum() == 1,
                ()->{})));
        }
    }

    @Getter
    @Setter
    public static class FairyWisp extends FollowCard {
        public Integer cost = 0;
        public String name = "妖精萤火";
        public String job = "妖精";
        private List<String> race = Lists.ofStr("妖精");
        public String mark = """
        战吼：如果本回合使用的卡牌数大于2,则本随从消失
        """;
        public String subMark = "本回合使用的卡牌数等于{}";

        public String getSubMark() {
            return subMark.replaceAll("\\{}",ownerPlayer().getCount(PLAY_NUM)+"");
        }

        public int atk = 1;
        public int hp = 1;

        public FairyWisp() {
            setMaxHp(getHp());
            setPlay(new Play(()->{
                if(ownerPlayer().getCount(PLAY_NUM)>=2) info.exile(this);
            }));
        }

    }

}
