package org.example.card.ccg.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.card.ccg.nemesis.Yuwan;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class ArtifactCall extends SpellCard {
    public Integer cost = 2;
    public String name = "创造物的呼唤";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("");
    public String mark = """
        抽1张创造物卡。
        如果共鸣状态已发动，则变为抽2张。
        如果共鸣状态已发动，且手牌为空，则变为抽3张
        """;

    public String subMark = "";
    public void init() {

        setPlay(new Play(
            ()->{
                if(ownerPlayer().getDeck().size()%2==0){
                    if(ownerPlayer().getHand().size()==0)
                        ownerPlayer().draw(p->p.hasRace("创造物"),3);
                    else
                        ownerPlayer().draw(p->p.hasRace("创造物"),2);
                }else
                    ownerPlayer().draw(p->p.hasRace("创造物"),1);
            }));
    }

}
