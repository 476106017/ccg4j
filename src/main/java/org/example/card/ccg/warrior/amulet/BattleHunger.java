package org.example.card.ccg.warrior.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.STRENGTH;
import org.example.constant.CardRarity;

@Getter
@Setter
public class BattleHunger extends AmuletCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "战斗饥渴";
    public String job = "战士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        回合结束时：强迫未攻击的随从攻击自己的主战者
        """;

    public String subMark = "";

    public void init() {
        addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->
            info.getAreaFollowsCopy().forEach(areaCard -> {
                if(areaCard.atArea() && areaCard instanceof FollowCard followCard){
                    followCard.attack(followCard.ownerLeader());
                }
            })
        )));
    }
}
