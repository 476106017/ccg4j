package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardType;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.GameObj;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public abstract class FollowCard extends AreaCard{
    public final CardType TYPE = CardType.FOLLOW;
    private int atk = 0;
    private int hp = 0;
    private int maxHp = 0;
    private boolean needSettle = false; // 是否需要结算
    private int turnAge = 0;
    private int turnAttackMax = 1;
    private int turnAttack = 0;
    private EquipmentCard equipment;
    private Damage incommingDamage = null;

    public void setIncommingDamage(Damage incommingDamage) {
//        info.msg(incommingDamage.getFrom().getId()+"对"+incommingDamage.getTo().getId()+"造成的伤害效果已被记录");
        info.getIncommingDamages().add(incommingDamage);
    }
    @Override
    public String getType() {
        return TYPE.getName();
    }

    public boolean notAttacked(){
        return info.thisPlayer()==ownerPlayer() && //是自己的回合
            getTurnAttack() < getTurnAttackMax() && // 回合可攻击数没有打满
            !hasKeyword("缴械") && !hasKeyword("眩晕") && !hasKeyword("冻结") &&
            (getTurnAge()>0 || hasKeyword("突进") || hasKeyword("疾驰"));
    }

    public void equip(EquipmentCard equipmentCard){
        // 解除之前的装备
        if(equipped()){
            getEquipment().death();
            if(!atArea()){
                info.msg(getNameWithOwner()+"没来得及装备"+equipmentCard.getId());
                equipmentCard.death();
                return;
            }
        }
        info.msg(getNameWithOwner() + "成功装备了" + equipmentCard.getId());
        setEquipment(equipmentCard);
        if(equipmentCard.isControl() && getOwner()!=equipmentCard.getOwner()){
            info.msg(getNameWithOwner() + "被"+enemyPlayer().getName()+"控制！");
            removeWhenAtArea();
            setOwner(equipmentCard.getOwner());
            addKeyword("被控制");
            ownerPlayer().addArea(this);

        }
        addKeywords(equipmentCard.getKeywords());
        addStatus(equipmentCard.getAddAtk(),equipmentCard.getAddHp());
        equipmentCard.setTarget(this);
        equipmentCard.tempEffects(EffectTiming.Entering);
    }
    public boolean equipped(){
        return getEquipment()!=null;
    }

    public boolean equipmentNamed(String s){
        return equipped() && getEquipment().getName().equals(s);
    }

    public void expireEquip(){
        int canUse = getEquipment().getCountdown();
        if (canUse > 0){
            getEquipment().setCountdown(canUse-1);
        }
    }
    public void expireEquipSettlement(){
        if(atArea()){
            int canUse = getEquipment().getCountdown();
            if(canUse == 0){
                // 用完了销毁
                getEquipment().death();
            }
        }
    }

    public void heal(int hp){
        if(!atArea())return;
        if(hasKeyword("无法回复")){
            info.msg(this.getNameWithOwner()+"无法回复生命值！（剩余"+this.getHp()+"点生命值）");
            return;
        }
        if(hp>0) {
            int oldHp = getHp();
            setHp(Math.min(getMaxHp(), getHp() + hp));
            info.msg(this.getNameWithOwner() + "回复" + (getHp()-oldHp) + "点（剩余" + this.getHp() + "点生命值）");
        }else {
            info.msg(this.getNameWithOwner() + "没有回复生命值（剩余" + this.getHp() + "点生命值）");
        }
    }
    public void purify(){
        if(!atArea())return;
        info.msg(this.getNameWithOwner()+"被沉默！");
        getKeywords().clear();
        List<Effect> noLongerAtArea = new ArrayList<>(getEffects(EffectTiming.WhenNoLongerAtArea));
        getEffects().clear();
        noLongerAtArea.forEach(effect -> effect.getEffect().accept(null));
    }
    public void addStatus(int atk, int hp){
        if(atk==0 && hp==0)return;
        // region 构造消息
        StringBuilder sb = new StringBuilder();
        sb.append(this.getNameWithOwner()).append("获得了");
        if(atk>0)sb.append("+");
        sb.append(atk).append("/");
        if(hp>0)sb.append("+");
        sb.append(hp);
        info.msg(sb.toString());
        // endregion 构造消息

        int finalAtk = this.getAtk()+atk;
        int finalHp = this.getHp()+hp;
        int finalMaxHp = this.getMaxHp()+hp;
        setAtk(Math.max(0,finalAtk));
        setHp(finalHp);
        setMaxHp(finalMaxHp);
        if(getHp()<=0){
            death();
        }
    }

    public void turnAttackOnce(){
        setTurnAttack(getTurnAttack()+1);
    }

    public void attack(GameObj target){
        info.msg(getNameWithOwner()+"攻击了对手的"+target.getId()+"！");

        turnAttackOnce();// 总之攻击过一次

        Damage damage = new Damage(this,target);

        ownerLeader().useEffectsAndSettle(EffectTiming.WhenAttack,damage);

        if(damage.checkFollowAtArea()){
            useEffectsAndSettle(EffectTiming.WhenAttack,damage);
            if(equipped()){
                getEquipment().useEffectsAndSettle(EffectTiming.WhenAttack,damage);
            }
        }

        if(damage.checkFollowAtArea() && damage.getTo() instanceof FollowCard)
            useEffectsAndSettle(EffectTiming.WhenBattle,damage);

        if(damage.checkFollowAtArea() && damage.getTo() instanceof FollowCard toFollow)
            toFollow.useEffectsAndSettle(EffectTiming.WhenBattle,damage);

        info.damageAttacking(this,target);

        if(equipped())expireEquipSettlement();

        info.pushInfo();

    }
}
