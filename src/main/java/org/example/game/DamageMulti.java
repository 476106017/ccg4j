package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.springframework.cglib.transform.impl.FieldProvider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 同步伤害
 */
@Getter
@Setter
public class DamageMulti {
    private GameInfo info;
    private List<Damage> damages;

    public DamageMulti(GameInfo info, List<Damage> damages) {
        this.info = info;
        this.damages = damages;
    }

    public void apply(){
        // region 受到伤害
        // 主战者受伤前
        damages.forEach(damage -> {
            if(damage.getTo() instanceof Leader leader){
                leader.getInfo().getAreaCardsCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.BeforeLeaderDamaged,damage));
                leader.useEffects(EffectTiming.BeforeLeaderDamaged,damage);
            }
        });
        damages.removeIf(damage -> damage.getTo() instanceof FollowCard toFollow && !toFollow.atArea());
        // 受伤扣血
        damages.forEach(damage -> {
            if(damage.getTo() instanceof FollowCard toFollow){
                damage.reduce();
                toFollow.setHp(toFollow.getHp() - damage.getDamage());
            }
            else if (damage.getTo() instanceof Leader leader){
                leader.ownerPlayer().setHp(leader.ownerPlayer().getHp()- damage.getDamage());
            }
        });
        // 受伤时
        damages.forEach(damage -> {
            if(damage.getTo() instanceof FollowCard toFollow){
                toFollow.tempEffects(EffectTiming.AfterDamaged,damage);
            }
            else if (damage.getTo() instanceof Leader leader){
                leader.damaged(damage);
                leader.getInfo().getAreaCardsCopy().forEach(areaCard -> areaCard.tempEffects(EffectTiming.AfterLeaderDamaged,damage));
                leader.tempEffects(EffectTiming.AfterLeaderDamaged,damage);
            }
        });
        info.startEffect();

        damages.forEach(damage -> {
            if(damage.getTo() instanceof FollowCard toFollow){
                if(damage.getFrom() instanceof Card card && card.hasKeyword("剧毒")) {
                    // 剧毒伤害击杀
                    info.msg(card.getNameWithOwner() + "发动剧毒效果！");
                    toFollow.destroyedBy(damage.getFrom());
                }else if(toFollow.getHp()<=0){
                    // 终结伤害击杀
                    toFollow.destroyedBy(damage.getFrom());
                }
            }
        });
        info.startEffect();

        // endregion
        // region 受伤效果
        // endregion
    }
}
