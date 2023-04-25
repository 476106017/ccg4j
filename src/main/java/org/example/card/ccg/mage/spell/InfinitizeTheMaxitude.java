package org.example.card.ccg.mage.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class InfinitizeTheMaxitude extends SpellCard {
    public Integer cost = 2;
    public String name = "巅峰无限";
    public String job = "法师";
    private List<String> race = Lists.ofStr();
    public String mark = """
        发现1张法术，压轴：回合结束时回到手牌
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            ownerPlayer().discoverCard(card -> card instanceof SpellCard,
                prototype2-> ownerPlayer().addHand(prototype2.copyBy(ownerPlayer())));
            if(ownerPlayer().getPpNum()==0){
                addEffects((new Effect(this,this,
                    EffectTiming.EndTurn,1, obj -> ownerPlayer().addHand(createCard(getClass())))));
            }
        }));
    }
}
