package org.example.card.ccg.vampire.follow;

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
public class ParaceliseDemonOfGreed extends FollowCard {
    private String name = "贪婪的大恶魔·帕拉琎丽婕";
    private Integer cost = 4;
    private int atk = 3;
    private int hp = 3;
    private String job = "吸血鬼";
    private List<String> race = Lists.ofStr("宴乐");
    private String mark = """
        被弃牌时：增加2张贪婪的大恶魔·帕拉璱丽婕卡片到牌堆中。
        之后如果手牌张数为0，则给予敌方的主战者2点伤害，回复自己的主战者2点生命值。
        瞬念召唤：回合开始时手牌张数为0
        入场时：给予敌方的主战者2点伤害。回复自己的主战者2点生命值。抽取1张卡片。
        """;
    private String subMark = "";

    public ParaceliseDemonOfGreed() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.WhenAbandoned,
            ()->{
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(ParaceliseDemonOfGreed.class));
                addCards.add(createCard(ParaceliseDemonOfGreed.class));
                ownerPlayer().addDeck(addCards);
                if(ownerPlayer().getHand().isEmpty()){
                    info.damageEffect(this,enemyLeader(),2);
                    ownerPlayer().heal(2);
                }
            }
        )));
        addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
            ()->ownerPlayer().getHand().isEmpty(),
            ()->{
                // 有bug。手动触发入场时
                info.damageEffect(this,enemyLeader(),2);
                ownerPlayer().heal(2);
                ownerPlayer().draw(1);
        }
        )));

        addEffects((new Effect(this,this, EffectTiming.Entering, ()->{
            info.damageEffect(this,enemyLeader(),2);
            ownerPlayer().heal(2);
            ownerPlayer().draw(1);
        })));
    }
}
