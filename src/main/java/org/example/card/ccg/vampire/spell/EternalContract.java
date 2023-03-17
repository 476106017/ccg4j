package org.example.card.ccg.vampire.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.card.ccg.hunter.follow.BattyGuest;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class EternalContract extends SpellCard {
    public Integer cost = 1;
    public String name = "无尽契约";
    public String job = "吸血鬼";
    private List<String> race = Lists.ofStr();
    public String mark = """
        抽取2张卡片。
        本回合结束时，随机舍弃2张自己的手牌。
        """;

    public String subMark = "";


    public EternalContract() {
        setPlay(new Play(()->{
            ownerPlayer().draw(2);
            ownerLeader().addEffect(new Effect(this,this, EffectTiming.EndTurn,2,
                ()->ownerPlayer().abandon(2)),false);
        }));
    }

}
