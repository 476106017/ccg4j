package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class Than0S extends FollowCard {
    private String name = "灭霸-0S";
    private Integer cost = 12;
    private int atk = 8;
    private int hp = 8;
    private String job = "中立";
    private List<String> race = Lists.ofStr("机械");
    private String mark = """
        在手牌上回合开始时：如果敌方剩余PP>0，则费用-3
        战吼：打个响指，双方舍弃一半的手牌，从牌堆移除一半的卡牌到墓地
        """;
    private String subMark = "";

    public Than0S() {
        setMaxHp(getHp());
        addEffects(new Effect(this,this, EffectTiming.BeginTurnAtHand,
            ()-> atHand() && enemyPlayer().getPpNum()>0,
            ()-> addCost(-3)));
        setPlay(new Play(()->
        {
            List<Card> handCopy1 = ownerPlayer().getHandCopy();
            ownerPlayer().abandon(handCopy1.subList(0,handCopy1.size()/2));
            List<Card> handCopy2 = enemyPlayer().getHandCopy();
            enemyPlayer().abandon(handCopy2.subList(0,handCopy2.size()/2));

            List<Card> deckCopy1 = ownerPlayer().getDeckCopy();
            List<Card> deckHalf1 = deckCopy1.subList(0, deckCopy1.size() / 2);
            ownerPlayer().getDeck().removeAll(deckHalf1);
            ownerPlayer().addGraveyard(deckHalf1);

            List<Card> deckCopy2 = enemyPlayer().getDeckCopy();
            List<Card> deckHalf2 = deckCopy2.subList(0, deckCopy2.size() / 2);
            enemyPlayer().getDeck().removeAll(deckHalf2);
            enemyPlayer().addGraveyard(deckHalf2);
        }));

    }
}