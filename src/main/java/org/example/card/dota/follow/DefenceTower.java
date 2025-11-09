package org.example.card.dota.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class DefenceTower extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "防御塔";
    private Integer cost = 4;
    private int atk = 0;
    private int hp = 12;
    private String job = "dota";
    private List<String> race = Lists.ofStr();
    private String mark = """
        回合结束时，随机对敌方场上1个随从造成2点伤害
        """;
    public String subMark = "";


    public void init() {
        setMaxHp(getHp());
        getKeywords().add("缴械");
        addEffects((new Effect(this,this, EffectTiming.EndTurn, ()->{
            AreaCard areaRandomFollow = enemyPlayer().getAreaRandomFollow();
            if(areaRandomFollow!=null)
                new Damage(this,areaRandomFollow,2).apply();
        })));
    }
}
