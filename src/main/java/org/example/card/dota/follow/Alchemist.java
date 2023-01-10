package org.example.card.dota.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.*;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class Alchemist extends FollowCard {
    private String name = "炼金术士";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 3;
    private String job = "dota";
    private List<String> race = Lists.ofStr("近卫军团");
    private String mark = """
        击杀时：搜索1张装备并使其费用-1
        回合开始时：如果是入场后第3/6/9个回合，给予任意1个敌方随从3层【眩晕】（如无目标则给自己）
        """;
    public String subMark = "入场后回合数：{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",(getTurnAge()+1)+"");
    }


    public Alchemist() {
        setMaxHp(getHp());
        addEffects(new Effect(this,this,EffectTiming.WhenKill,obj->{
            Card draw = ownerPlayer().draw(card -> card instanceof EquipmentCard);
            if(draw !=null && draw.getCost()>0){
                draw.setCost(draw.getCost() - 1);
            }
        }));
        addEffects(new Effect(this,this,EffectTiming.BeginTurn,obj->{
            if(getTurnAge() == 2 || getTurnAge() == 5 || getTurnAge() == 8){
                AreaCard areaRandomFollow = enemyPlayer().getAreaRandomFollow();
                if(areaRandomFollow!=null)
                    areaRandomFollow.addKeywordN("眩晕",3);
                else
                    addKeywordN("眩晕",3);
            }
        }));
    }
}