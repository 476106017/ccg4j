package org.example.card.ccg.neutral.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class MinimizeWeaknesses extends AmuletCard {


   private CardRarity rarity = CardRarity.GOLD;
    public Integer cost = 7;

    public String name = "弱点最小化";
    public String job = "中立";
    private List<String> race = Lists.ofStr();

    public String mark = """
        我方主战者无法被超杀
        """;
    public String subMark = "";

    List<FollowCard> effectFollows = new ArrayList<>();

    public void init() {
        setCountDown(3);
        addEffects((new Effect(this,this,
            EffectTiming.AfterLeaderDamaged,obj->ownerPlayer().getHp()<0,
            obj -> {
                final Damage damage = (Damage) obj;
                info.msg(getNameWithOwner()+"发动效果！");
                ownerPlayer().heal(damage.getDamage());
            })));
    }

}
