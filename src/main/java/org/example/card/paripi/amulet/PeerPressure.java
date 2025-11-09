package org.example.card.paripi.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class PeerPressure extends AmuletCard {


   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 5;

    public String name = "同调压力";
    public String job = "派对咖";
    private List<String> race = Lists.ofStr();

    public String mark = """
        我方出牌时：如果使用的是战吼随从牌，则对敌方所有战吼随从造成1点伤害，对敌方所有非战吼随从造成2点伤害
        若敌方场上无随从，则对敌方主战者造成2点伤害
        """;
    public String subMark = "";


    public void init() {

        addEffects((new Effect(this,this, EffectTiming.WhenPlay,
            obj-> obj instanceof FollowCard followCard && followCard.getPlay()!=null,
            obj->{
                List<FollowCard> follows = enemyPlayer().getAreaFollowsAsFollow();
                // 无随从打主战者
                if(follows.isEmpty()){
                    info.damageEffect(this,enemyLeader(),2);
                    return;
                }
                // 有随从看是不是战吼随从
                follows.forEach(followCard -> {
                    if (followCard.getPlay()!=null) {
                        info.damageEffect(this,followCard,2);
                    }else {
                        info.damageEffect(this,followCard,1);
                    }
                });
            })));
    }

}
