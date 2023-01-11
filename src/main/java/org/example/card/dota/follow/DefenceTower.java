package org.example.card.dota.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.EquipmentCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;

@Getter
@Setter
public class DefenceTower extends FollowCard {
    private String name = "防御塔";
    private Integer cost = 3;
    private int atk = 0;
    private int hp = 12;
    private String job = "dota";
    private List<String> race = Lists.ofStr();
    private String mark = """
        回合结束时，随机对敌方场上1个随从造成2点伤害
        """;
    public String subMark = "";


    public DefenceTower() {
        setMaxHp(getHp());
        getKeywords().add("缴械");
        addEffects((new Effect(this,this, EffectTiming.EndTurn, ()->{
            AreaCard areaRandomFollow = enemyPlayer().getAreaRandomFollow();
            new Damage(this,areaRandomFollow,2).apply();
        })));
    }
}