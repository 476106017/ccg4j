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
                // 攻击方是随从，计算关键词
                if (damage.getFrom() instanceof FollowCard fromFollow) {
                    // 普攻伤害消耗攻击方装备耐久
                    if(damage.isFromAtk() && fromFollow.equipped())
                        fromFollow.expireEquip();
                    if(fromFollow.hasKeyword("重伤")){
                        info.msg(fromFollow.getNameWithOwner() + "发动重伤效果！");
                        toFollow.addKeyword("无法回复");
                    }
                    if(fromFollow.hasKeyword("自愈")){
                        info.msg(fromFollow.getNameWithOwner() + "发动自愈效果！");
                        fromFollow.heal(damage.getDamage());
                    }
                    if(fromFollow.hasKeyword("吸血")){
                        info.msg(fromFollow.getNameWithOwner() + "发动吸血效果！");
                        fromFollow.ownerPlayer().heal(damage.getDamage());
                    }
                }
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
