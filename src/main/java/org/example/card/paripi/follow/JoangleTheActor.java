package org.example.card.paripi.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.paripi.Kongming;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class JoangleTheActor extends FollowCard {
    private String name = "演员乔安格";
    private Integer cost = 3;
    private int atk = 1;
    private int hp = 4;
    private String job = "派对咖";
    private List<String> race = Lists.ofStr();
    private String mark = """
        受伤时：触发该随从全部亡语
        亡语：派对热度+1
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->
            ownerPlayer().draw(card -> card.getCost().equals(ownerPlayer().getPpNum()))));

        addEffects((new Effect(this,this, EffectTiming.AfterDamaged, ()->{
            getEffects(EffectTiming.DeathRattle).forEach(effect -> effect.getEffect().accept(null));
        })));
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, ()->{
            if(ownerLeader() instanceof Kongming kongming)
                kongming.addPartyHot(1);
        })));
    }
}