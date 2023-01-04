package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;

@Getter
@Setter
public class Damage{
    GameObj from;
    GameObj to;
    int damage;
    boolean isCounter = false;
    boolean isFromAtk = false;

    public Damage(GameObj from, GameObj to, int damage) {
        this.from = from;
        this.to = to;
        this.damage = damage;
    }
    public Damage(GameObj from, GameObj to) {
        assert from instanceof FollowCard;
        this.from = from;
        this.to = to;
        this.damage = ((FollowCard) from).getAtk();
        this.isFromAtk = true;
    }

    public GameObj another(GameObj obj){
        if(getFrom() == obj)
            return getTo();
        return getFrom();
    }

    public boolean checkFollowAtArea(){
        if(getFrom() instanceof FollowCard fromFollow && !fromFollow.atArea())
            return false;
        if(getTo() instanceof FollowCard toFollow && !toFollow.atArea())
            return false;
        return true;
    }

    public void reduce(){
        if(to instanceof FollowCard toFollow && toFollow.atArea()) {
            if (!isFromAtk && toFollow.hasKeyword("效果伤害免疫")) {
                setDamage(0);
                toFollow.getInfo().msg(toFollow.getNameWithOwner() + "免疫了效果伤害！");
            } else {
                int reduce = 0;
                reduce += toFollow.countKeyword("伤害减免");
                if (isFromAtk())
                    reduce += toFollow.countKeyword("护甲");
                else
                    reduce += toFollow.countKeyword("魔抗");

                if (reduce > 0) {
                    int finalReduce = Math.min(getDamage(), reduce);
                    toFollow.getInfo().msg(toFollow.getNameWithOwner() + "减少了" + finalReduce + "点伤害");
                    setDamage(getDamage() - finalReduce);
                }
            }
        }
    }

    public void settlement(){
        GameInfo info = to.getInfo();
        if(to instanceof Leader toLeader){

            PlayerInfo toPlayer = toLeader.ownerPlayer();

            toLeader.useEffectWithDamage(EffectTiming.AfterLeaderDamaged,this);

        }




        if(!atArea()) return;
        GameObj from = getFrom();
        boolean fromAtk = isFromAtk();
        assert !fromAtk || from instanceof FollowCard; // 非a或b，即非(a且非b)，a出现时一定不是“非b”

        // 攻击方是随从，计算关键词
        if (from instanceof FollowCard fromFollow) {
            // 普攻伤害消耗攻击方装备耐久
            if(fromAtk && fromFollow.equipped())
                fromFollow.expireEquip();
            if(fromFollow.hasKeyword("重伤")){
                info.msg(fromFollow.getNameWithOwner() + "发动重伤效果！");
                addKeyword("无法回复");
            }
            if(hasKeyword("重伤")){
                info.msg(getNameWithOwner() + "发动重伤效果！");
                fromFollow.addKeyword("无法回复");
            }
            if(fromFollow.hasKeyword("自愈")){
                info.msg(fromFollow.getNameWithOwner() + "发动自愈效果！");
                fromFollow.heal(getDamage());
            }
            // 普攻伤害反击
            if(fromAtk && hasKeyword("自愈")){
                info.msg(getNameWithOwner() + "发动自愈效果！(反击)");
                heal(getCountDamage());
            }
            if(fromFollow.hasKeyword("吸血")){
                info.msg(fromFollow.getNameWithOwner() + "发动吸血效果！");
                fromFollow.ownerPlayer().heal(getDamage());
            }
            // 普攻伤害反击
            if(fromAtk && hasKeyword("吸血")){
                info.msg(getNameWithOwner() + "发动吸血效果！(反击)");
                ownerPlayer().heal(getCountDamage());
            }

        }
        // 结算本随从
        if(getHp() <= 0){
            if(from instanceof Card fromCard
                && !fromCard.getWhenKills().isEmpty()){
                info.msg(fromCard.getNameWithOwner() + "发动击杀时效果！");
                fromCard.getWhenKills().forEach(whenKill -> whenKill.effect().accept(this));
            }
            death();
        }else {
            if(!getAfterDamageds().isEmpty()){
                info.msg(getNameWithOwner() + "发动受伤时效果！");
                getAfterDamageds().forEach(afterDamaged -> afterDamaged.effect().accept(damage));
            }

        }
        // 结算反击随从
        if(from instanceof FollowCard fromFollow && fromFollow.atArea()){
            if(fromFollow.getHp() <= 0){
                fromFollow.death();
            }else {
                if(!fromFollow.getAfterDamageds().isEmpty()){
                    info.msg(fromFollow.getNameWithOwner() + "发动受伤时效果！");
                    fromFollow.getAfterDamageds().forEach(afterDamaged -> afterDamaged.effect().accept(damage));
                }
            }

            // 计算剧毒效果
            // region 先记录剧毒效果，再破坏（不要先后计算剧毒效果）
            boolean destroyThis = false, destroyFrom = false;
            if (atArea() && fromFollow.hasKeyword("剧毒")) {
                info.msg(fromFollow.getNameWithOwner() + "发动剧毒效果！");
                destroyThis = true;
            }
            // 普攻伤害才反击
            if (fromAtk && fromFollow.atArea() && hasKeyword("剧毒")) {
                info.msg(getNameWithOwner() + "发动剧毒效果！(反击)");
                destroyFrom = true;
            }
            if (destroyThis)
                fromFollow.destroy(this);
            if (destroyFrom)
                destroy(fromFollow);
            // endregion
        }
    }
}
