package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;

import java.util.ArrayList;
import java.util.List;

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
        this.damages = new ArrayList<>(damages);
    }

    public void apply(){
        // 受伤前
        damages.forEach(damage -> {
            damage.getTo().getInfo().getAreaCardsCopy().forEach(areaCard ->
                areaCard.useEffects(EffectTiming.BeforeDamaged,damage));
            damage.getTo().useEffects(EffectTiming.BeforeDamaged,damage);
        });
        damages.removeIf(damage -> damage.getTo() instanceof FollowCard toFollow && !toFollow.atArea());
        // region 扣血
        damages.forEach(damage -> {
            GameObj to = damage.getTo();
            if(to instanceof FollowCard toFollow){
                damage.reduce();
                toFollow.setHp(toFollow.getHp() - damage.getDamage());
                info.msg(to.getNameWithOwner()+"受到了来自"+damage.getFrom().getNameWithOwner()+"的"+damage.getDamage()+"点伤害！" +
                    "（剩余"+ toFollow.getHp()+"点生命值）");
            }else if (to instanceof Leader leader){
                leader.ownerPlayer().setHp(leader.ownerPlayer().getHp()- damage.getDamage());
                info.msg(to.getNameWithOwner()+"受到了来自"+damage.getFrom().getNameWithOwner()+"的"+damage.getDamage()+"点伤害！" +
                    "（剩余"+ leader.getHp()+"点生命值）");
            }
        });
        // endregion 扣血
        // region 受伤
        damages.forEach(damage -> {
            if(damage.getTo() instanceof FollowCard toFollow && toFollow.atArea()){
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
                toFollow.setIncommingDamage(damage);
            }else if (damage.getTo() instanceof Leader leader){
                leader.getInfo().getAreaCardsCopy().forEach(areaCard -> areaCard.tempEffects(EffectTiming.AfterLeaderDamaged,damage));
                leader.tempEffects(EffectTiming.AfterLeaderDamaged,damage);
            }
        });
        // endregion 受伤


        // region 破坏
        damages.forEach(damage -> {
            if(damage.getTo() instanceof FollowCard toFollow && toFollow.atArea()){
                if(damage.getFrom() instanceof Card card && card.hasKeyword("剧毒")) {
                    // 剧毒伤害击杀
                    info.msg(card.getNameWithOwner() + "发动剧毒效果！");
                    toFollow.setDestroyedBy(damage.getFrom());
                }else if(toFollow.getHp()<=0){
                    // 终结伤害击杀
                    toFollow.setDestroyedBy(damage.getFrom());
                }
            }
        });
        // endregion 结算
        info.startEffect();

    }
}
