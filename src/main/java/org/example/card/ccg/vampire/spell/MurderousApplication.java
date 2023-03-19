package org.example.card.ccg.vampire.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.ccg.vampire.follow.ParaceliseDemonOfGreed;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MurderousApplication extends SpellCard {
    public Integer cost = 1;
    public String name = "涂抹杀意";
    public String job = "吸血鬼";
    private List<String> race = Lists.ofStr();
    public String mark = """
        被舍弃时：下个自己的回合开始时，抽取1张卡片。
        给予1个自己的吸血鬼从者剧毒效果。
        """;

    public String subMark = "";


    public MurderousApplication() {
        addEffects((new Effect(this,this, EffectTiming.WhenAbandoned,
            ()-> ownerLeader().addEffect(new Effect(this,this, EffectTiming.BeginTurn,3,
                ()->ownerPlayer().draw(1)),false)
        )));
        setPlay(new Play(()-> ownerPlayer().getAreaFollowsAsGameObjBy(p->p.getJob().equals("吸血鬼")),
            true,
            target->{
                FollowCard followCard = (FollowCard) target;
                followCard.addKeyword("剧毒");
            }));
    }

}
