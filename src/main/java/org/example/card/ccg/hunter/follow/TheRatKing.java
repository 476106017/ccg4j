package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;

@Getter
@Setter
public class TheRatKing extends FollowCard {
    private String name = "鼠王";
    private Integer cost = 5;
    private int atk = 5;
    private int hp = 5;
    private String job = "猎人";
    private List<String> race = Lists.ofStr("野兽");
    private String mark = """
        亡语：主战者获得唯一效果【注能(5)：召还鼠王，并失去该效果】
        """;
    private String subMark = "";

    public TheRatKing() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            ownerLeader().addEffect(new Effect(
                this,ownerLeader(), EffectTiming.Charge, card ->{
                    if(card instanceof FollowCard){
                        ownerPlayer().count("RAT_KING");
                        if(ownerPlayer().getCount("RAT_KING")>=5){
                            List<Card> shuwang = ownerPlayer().getGraveyard().stream().filter(card1 ->
                                card1 instanceof FollowCard && card1.getName().equals("鼠王")).toList();
                            if(!shuwang.isEmpty()){
                                ownerPlayer().recall((AreaCard) shuwang.get(0));
                            }
                            // 失去该效果
                            ownerLeader().getEffects().removeAll(ownerLeader().getEffectsFrom(this));
                        }
                    }
            }),true);
        })));
    }

}