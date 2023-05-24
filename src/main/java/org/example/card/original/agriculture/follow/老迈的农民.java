package org.example.card.original.agriculture.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.ccg.nemesis.spell.CalamitysGenesis;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class 老迈的农民 extends FollowCard {
    public static final int APPOSITION = 7;

    private String name = "老迈的农民";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "农业";
    private List<String> race = Lists.ofStr();
    private String mark = """
        （在编辑卡组时，可同时携带7张）
        回合开始时：死亡
        亡语：将1张【年轻的农民】洗入牌堆。
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects(new Effect(this,this, EffectTiming.BeginTurn, obj->{
            death();
        }));
        addEffects(new Effect(this,this, EffectTiming.DeathRattle, obj->{
            List<Card> addCards = new ArrayList<>();
            addCards.add(createCard(年轻的农民.class));
            ownerPlayer().addDeck(addCards);
        }));
    }

    @Getter
    @Setter
    public static class 年轻的农民 extends FollowCard {
        private String name = "年轻的农民";
        private Integer cost = 1;
        private int atk = 1;
        private int hp = 1;
        private String job = "农业";
        private List<String> race = Lists.ofStr();
        private String mark = """
            瞬念召唤：回合开始时，pp最大值+1
            回合开始时：使自己主战者回复3点生命,变身成【年迈的农民】
            """;
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
            addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
                ()->true,
                ()-> ownerPlayer().addPpMax(1)
            )));
            addEffects(new Effect(this, this, EffectTiming.BeginTurn, obj -> {
                ownerPlayer().heal(3);
                info.transform(this,createCard(老迈的农民.class));
            }));
        }
    }
}