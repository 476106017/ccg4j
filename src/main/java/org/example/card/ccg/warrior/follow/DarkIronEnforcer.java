package org.example.card.ccg.warrior.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class DarkIronEnforcer extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "黑铁执行者";
    private Integer cost = 6;
    private int atk = 6;
    private int hp = 6;
    private String job = "战士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        受伤时：随机对1个敌方随从造成等量伤害
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());

        addEffects(new Effect(this,this, EffectTiming.AfterDamaged,obj->{
            Damage damage = (Damage) obj;
            info.damageEffect(this,Lists.randOf(enemyPlayer().getAreaFollowsAsFollow()), damage.getDamage());
        }));
    }
}
