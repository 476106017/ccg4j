package org.example.morecard.genshin.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.morecard.genshin.LittlePrincess;
import org.example.morecard.genshin.system.ElementCostSpellCard;
import org.example.morecard.genshin.system.Elemental;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class LuckyDay extends ElementCostSpellCard {
    public List<Elemental> elementCost = List.of(Elemental.Void);
    public String name = "幸运日";
    public String job = "原神";
    private List<String> race = Lists.ofStr();
    public String mark = """
    瞬念召唤：回合开始时投出的（非万能元素）骰子拥有全部7种元素，获得游戏胜利
    增加1个万能元素骰，将此牌放回牌堆
    """;
    public String subMark = "";

    public LuckyDay() {
        addEffects(new Effect(this,this, EffectTiming.InvocationBegin,
            ()-> ownerLeader() instanceof LittlePrincess littlePrincess
                    && littlePrincess.diceTypeNum() == 7,
            ()-> info.gameset(ownerPlayer())));

        setPlay(new Play(()->{
            if(ownerLeader() instanceof LittlePrincess littlePrincess){
                littlePrincess.getElementDices().add(Elemental.Universal);
            }
            ownerPlayer().getHand().remove(this);
            ownerPlayer().getDeck().add(this);
        }));

    }
}