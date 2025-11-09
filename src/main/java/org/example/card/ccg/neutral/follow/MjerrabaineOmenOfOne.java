package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.Comparator;
import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class MjerrabaineOmenOfOne extends FollowCard {

   private CardRarity rarity = CardRarity.LEGENDARY;
    private String name = "唯我绝杰·马塞班恩";
    private Integer cost = 5;
    private int atk = 4;
    private int hp = 4;
    private String job = "中立";
    private List<String> race = Lists.ofStr("神");
    private String mark = """
        瞬念召唤：回合开始时牌堆没有重复卡片，返回手牌
        战吼：若牌堆没有重复卡片，则使我方主战者获得
        【回合结束时：如果己方场上仅有1个从者，则会随机给予1个自己的从者+2/+2效果。 随后，随机给予1个敌方的从者2点伤害，并给予敌方的主战者2点伤害】
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());

        addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
            ()->{
                final List<Card> deck = ownerPlayer().getDeck();
                final long count = deck.stream().map(GameObj::getName).distinct().count();
                return deck.size()==count;
            },
            ()->{
                backToHand();
            }
        )));

        setPlay(new Play(()->{
            final List<Card> deck = ownerPlayer().getDeck();
            final long count = deck.stream().map(GameObj::getName).distinct().count();
            if(deck.size()==count){
                ownerLeader().addEffect(new Effect(this,ownerLeader(), EffectTiming.EndTurn,
                    ()->ownerPlayer().getAreaFollows().size()==1,
                    ()->{
                        final AreaCard follow = ownerPlayer().getAreaRandomFollow();
                        if(follow instanceof FollowCard f){
                            f.addStatus(2,2);
                        }

                        final AreaCard enemyFollow = enemyPlayer().getAreaRandomFollow();
                        if(enemyFollow instanceof FollowCard f2){
                            info.damageEffect(this,f2,2);
                        }

                        info.damageEffect(this,enemyLeader(),2);
                    }), true);
            }
        }));
    }
}
