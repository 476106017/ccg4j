package org.example.card.ccg.sts;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.example.constant.CounterKey.BLOCK;
import static org.example.constant.CounterKey.EP_NUM;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Intruder extends Leader {


   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "入侵者";
    private String job = "杀戮尖塔";

    private String mark = """
        游戏开始时：对手增加100点生命，自己的杀戮尖塔职业牌费用-1
        回合开始时：再抽4张牌,pp值变成3,格挡消失
        回合结束时：将剩余手牌放置于牌堆底部
        """;
    private String skillName = "休息处";
    private String skillMark =  """
        选择一张手牌，如果是诅咒，则除外；
        如果可以升级，则升级；
        否则回复主战者20%的hp
        """;
    private int skillCost = 2;

    private String overDrawMark =  """
        轮回你墓地的所有牌！
        """;

    private Consumer<Integer> overDraw = integer -> {
        final List<Card> graveyard = ownerPlayer().getGraveyard();
        if(graveyard.isEmpty()) return;
        ownerPlayer().transmigration(card -> true,graveyard.size());
    };

    @Override
    public void init() {
        addEffect(new Effect(this, this, EffectTiming.BeginGame,() -> {
                enemyPlayer().addHpMax(100);
                enemyPlayer().heal(100);
                ownerPlayer().getDeck().forEach(card -> {
                    if("杀戮尖塔".equals(card.getJob())){
                        card.addCost(-1);
                    }
                });
            }));
        addEffect(new Effect(this, this, EffectTiming.BeginTurn,() -> {
                ownerPlayer().draw(4);
                ownerPlayer().setPpNum(3);
                if (ownerPlayer().getCount("壁垒")==0) {
                    ownerPlayer().clearCount(BLOCK);
                }
            }));
        addEffect(new Effect(this, this, EffectTiming.EndTurn,() ->
            ownerPlayer().backToDeck(ownerPlayer().getHand())));
    }

    @Override
    public List<GameObj> targetable() {
        return ownerPlayer().getHandAsGameObj();
    }

    @Override
    public void skill(GameObj target) {
        super.skill(target);

        final Card card = (Card) target;
        if(card.hasRace("诅咒")){
            info.exile(card);
            return;
        }
        if(!card.isUpgrade()){
            card.upgrade();
            return;
        }
        final int hpMax = ownerPlayer().getHpMax();
        ownerPlayer().heal((int) (hpMax*0.2));

    }

}
