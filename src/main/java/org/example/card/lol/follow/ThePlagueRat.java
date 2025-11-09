package org.example.card.lol.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.POISON;
import org.example.constant.CardRarity;


@Getter
@Setter
public class ThePlagueRat extends FollowCard {

   private CardRarity rarity = CardRarity.SILVER;
    private String name = "瘟疫之源";
    private Integer cost = 3;
    private int atk = 1;
    private int hp = 1;
    private String job = "英雄联盟";
    private List<String> race = Lists.ofStr();
    private String mark = """
        攻击时：给予目标2层【中毒】
        """;
    private String subMark = "";

    private boolean turnDamaged = false;


    public void init() {
        setMaxHp(getHp());
        getKeywords().add("远程");
        getKeywords().add("疾驰");
        addEffects(new Effect(this,this, EffectTiming.WhenAttack, obj->{
            Damage damage = (Damage) obj;
            final GameObj to = damage.getTo();
            if(to instanceof FollowCard toFollow){
                toFollow.addKeywordN("中毒",2);
            }else if(to instanceof Leader leader){
                leader.ownerPlayer().count(POISON,2);
            }
        }));
    }
}
