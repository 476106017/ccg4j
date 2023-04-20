package org.example.card.ccg.rogue.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class SecretService extends AmuletCard {

    public Integer cost = 1;

    public String name = "特务机关";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();

    public String mark = """
        战吼：消耗敌方剩余pp，使本卡增加与之相等的倒数
        使用卡牌时：如果使用的卡牌费用不大于敌方剩余pp，则偷取与该费用相等的pp
        """;
    public String subMark = "";

    List<FollowCard> effectFollows = new ArrayList<>();

    public void init() {
        setCountDown(1);
        addEffects((new Effect(this,this,
            EffectTiming.WhenPlay, obj->{
                final Card card = (Card) obj;
                return card.getCost()<=enemyPlayer().getPpNum();
            },
            obj -> {
                final Card card = (Card) obj;
                final Integer cost = card.getCost();
                enemyPlayer().addPp(-cost);
                ownerPlayer().addPp(cost);
            })));
    }

}
