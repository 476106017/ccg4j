package org.example.card.other.rule.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class HenAndEgg extends SpellCard {
    public Integer cost = 2;
    public String name = "鸡与蛋的悖论";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr();
    public String mark = """
        召唤1只2/2的孵蛋机366型、和1只0/1的鸡蛋
        他们是由互相创造的
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(()->{
            HatchBot366 angryHen = createCard(HatchBot366.class);
            Egg egg = createCard(Egg.class);
            angryHen.changeParent(egg);
            egg.changeParent(angryHen);
            ownerPlayer().summon(angryHen);
            ownerPlayer().summon(egg);
        }));
    }

    @Getter
    @Setter
    public static class HatchBot366 extends FollowCard {
        private String name = "孵蛋机366型";
        private Integer cost = 2;
        private int atk = 2;
        private int hp = 2;
        private String job = "游戏规则";
        private List<String> race = Lists.ofStr("机械");
        private String mark = """
            战吼：使战场的全部鸡蛋变成孵蛋机366型
            """;
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
            setPlay(new Play(() -> {
                ownerPlayer().getAreaFollowsBy(followCard -> followCard instanceof Egg)
                    .forEach(areaCard -> info.transform(areaCard,createCard(HatchBot366.class)));
            }));
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
        private List<String> race = Lists.ofStr("机械");
        private String mark = "";
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
        }
    }
}
