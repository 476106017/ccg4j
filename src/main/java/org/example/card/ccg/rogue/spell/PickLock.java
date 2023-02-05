package org.example.card.ccg.rogue.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class PickLock extends SpellCard {
    public Integer cost = 3;
    public String name = "开锁";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        对敌方场上一名随从造成2点伤害
        超杀：除外该随从并抽1张牌
        """;

    public String subMark = "";

    public PickLock() {
        setPlay(new Play(
            () -> enemyPlayer().getAreaFollowsAsGameObj(),true,
            gameObjs -> {
                info.damageEffect(this, gameObjs,2);
            }));
        addEffects((new Effect(this,this, EffectTiming.WhenKill,
            obj -> ((FollowCard) obj).getHp() < 0,
            obj -> {
                info.exile(((FollowCard) obj));
                ownerPlayer().draw(1);
            })));
    }
}
