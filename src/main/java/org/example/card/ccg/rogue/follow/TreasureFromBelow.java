package org.example.card.ccg.rogue.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import java.util.function.Consumer;


@Getter
@Setter
public class TreasureFromBelow extends FollowCard {
    private String name = "深海的秘藏";
    private Integer cost = 0;
    private int atk = 0;
    private int hp = 2;
    private String job = "潜行者";
    private List<String> race = Lists.ofStr("神龛");
    private String mark = """
        瞬念召唤：回合开始时
        回合开始时：从你对手的牌库中偷取1张牌，其法力值消耗减少2点。
        """;
    private String subMark = "";
    public void init() {
        setMaxHp(getHp());

        addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
            ()->true,
            ()->ownerPlayer().steal(1).forEach(card -> card.addCost(-2))
        )));
        addEffects(new Effect(this,this, EffectTiming.BeginTurn,
            ()->ownerPlayer().steal(1).forEach(card -> card.addCost(-2))));

        addEffects((new Effect(this,this, EffectTiming.Leaving, obj->
            ownerPlayer().summon(createCard(TreasureLost.class))
        )));
    }


    @Getter
    @Setter
    public static class TreasureLost extends AmuletCard {
        public Integer cost = 0;
        public String name = "深海的秘藏（复活中）";
        public String job = "复仇者";
        private List<String> race = Lists.ofStr("神龛");
        public String mark = """
        复活中
        """;

        public String subMark = "";

        public void init() {
            setCountDown(2);

            addEffects((new Effect(this,this, EffectTiming.Leaving, obj-> {
                ownerPlayer().summon(createCard(TreasureFromBelow.class));
                ownerPlayer().steal(1).forEach(card -> card.addCost(-2));
            })));
        }
    }
}
