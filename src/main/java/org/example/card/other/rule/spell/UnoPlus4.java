package org.example.card.other.rule.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class UnoPlus4 extends SpellCard {
    public Integer cost = 4;
    public String name = "+4";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr();
    public String mark = """
        对手获得1张质疑、抽4张牌、并在下个回合只能使用质疑
        （对手可以质疑你手牌有费用小于等于4的牌）
        """;

    public String subMark = "";
    public int target = 2;

    public UnoPlus4() {
        setPlay(new Play(()->{
            enemyPlayer().addHand(createCard(UnoDoubt.class));
            enemyPlayer().draw(4);
            enemyPlayer().setHandPlayable(card -> card instanceof UnoDoubt);
        }));
    }


    @Getter
    @Setter
    public static class UnoDoubt extends SpellCard {
        public Integer cost = 0;
        public String name = "质疑";
        public String job = "游戏规则";
        private List<String> race = Lists.ofStr();
        public String mark = """
        在手牌上回合结束时：舍弃此卡
        —————————————
        质疑对手手牌有费用小于等于4的牌：
        若成功，自己可以使用卡牌，对手抽4张牌、并在下个回合无法使用卡牌；
        若失败，再抽2张牌；
        """;

        public String subMark = "";
        public int target = 2;

        public UnoDoubt() {
            addEffects(new Effect(this,this, EffectTiming.EndTurnAtHand,
                ()-> ownerPlayer().abandon(this)));
            setPlay(new Play(()->{
                boolean succ = enemyPlayer().getHandBy(card -> card.getCost() <= 4).size() > 0;
                if(succ){
                    info.msg("质疑成功！可以使用卡牌；对手抽4张牌、并在下个回合无法使用卡牌");
                    ownerPlayer().setHandPlayable(card -> true);
                    enemyPlayer().draw(4);
                    enemyPlayer().setHandPlayable(card -> false);
                }else {
                    info.msg("质疑失败！再抽2张牌");
                    ownerPlayer().draw(2);
                }
            }));
        }


    }

}
