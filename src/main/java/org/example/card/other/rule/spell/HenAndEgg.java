package org.example.card.other.rule.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class HenAndEgg extends SpellCard {
    public Integer cost = 2;
    public String name = "鸡与蛋的悖论";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr();
    public String mark = """
        召唤1只2/2的愤怒母鸡、和1只0/1的鸡蛋
        他们是由互相创造的
        """;

    public String subMark = "";

    public HenAndEgg() {
        setPlay(new Play(()->{
            AngryHen angryHen = createCard(AngryHen.class);
            Egg egg = createCard(Egg.class);
            angryHen.changeParent(egg);
            egg.changeParent(angryHen);
            ownerPlayer().summon(angryHen);
            ownerPlayer().summon(egg);
        }));
    }

    @Getter
    @Setter
    public static class AngryHen extends FollowCard {
        private String name = "愤怒母鸡";
        private Integer cost = 1;
        private int atk = 2;
        private int hp = 2;
        private String job = "游戏规则";
        private List<String> race = Lists.ofStr();
        private String mark = """
            """;
        private String subMark = "";

        public AngryHen() {
            setMaxHp(getHp());
        }
    }
    @Getter
    @Setter
    public static class Egg extends FollowCard {
        private String name = "鸡蛋";
        private Integer cost = 0;
        private int atk = 0;
        private int hp = 1;
        private String job = "游戏规则";
        private List<String> race = Lists.ofStr();
        private String mark = "";
        private String subMark = "";

        public Egg() {
            setMaxHp(getHp());
        }
    }
}
