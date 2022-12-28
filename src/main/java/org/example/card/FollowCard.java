package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardType;
import org.example.game.Damage;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.system.function.FunctionN;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


@Getter
@Setter
public abstract class FollowCard extends AreaCard{
    public final CardType TYPE = CardType.FOLLOW;
    private int atk = 0;
    private int hp = 0;
    private int maxHp = 0;
    private int turnAge = 0;
    private int turnAttackMax = 1;
    private int turnAttack = 0;
    private EquipmentCard equipment;

    // region 效果列表

    private List<Event.WhenAttack> whenAttacks = new ArrayList<>();
    private List<Event.WhenBattle> whenBattles = new ArrayList<>();
    private List<Event.WhenDamaged> whenDamageds = new ArrayList<>();
    private List<Event.WhenLeaderSkill> whenLeaderSkills = new ArrayList<>();
    // endregion 效果列表

    @Override
    public String getType() {
        return TYPE.getName();
    }

    public void equip(EquipmentCard equipmentCard){
        if(!equipmentCard.getTargetName().isEmpty() && !getName().equals(equipmentCard.getTargetName())){
            info.msgToThisPlayer(this.getName()+"无法装备"+equipmentCard.getName()+"！");
            return;
        };
        info.msg(getNameWithOwner() + "装备了" + equipmentCard.getName());
        setEquipment(equipmentCard);
        addKeywords(equipmentCard.getKeywords());
        addStatus(equipmentCard.getAddAtk(),equipmentCard.getAddHp());
        equipmentCard.setTarget(this);
        if(!equipmentCard.getEnterings().isEmpty()){
            info.msg(equipmentCard.getNameWithOwner() + "发动入场时效果！");
            equipmentCard.getEnterings().forEach(entering -> entering.effect().apply());// 发动入场时
        }
    }
    public boolean equipped(){
        return getEquipment()!=null;
    }
    public void expireEquip(){
        int canUse = getEquipment().getCountdown();
        if (canUse > 0){
            getEquipment().setCountdown(canUse-1);
        }
    }
    public void expireEquipSettlement(){
        if(!atArea()){
            info.msg(getEquipment().getNameWithOwner()+"随着装备者的离场而随之破坏了");
            getEquipment().death();
        }
        int canUse = getEquipment().getCountdown();
        if(canUse <= 0){
            // 用完了销毁
            getEquipment().death();
        }
    }

    public void heal(int hp){
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
        info.msg(this.getNameWithOwner()+"遭到了净化！");
        getKeywords().clear();

        getPlays().clear();
        getInvocationBegins().clear();
        getInvocationEnds().clear();
        getWhenKills().clear();
        getBoosts().clear();
        getCharges().clear();
        getTransmigrations().clear();
        getExiles().clear();

        getWhenBattles().clear();
        getWhenAttacks().clear();
        getWhenDamageds().clear();
        getWhenLeaderSkills().clear();

        getEffectBegins().clear();
        getEffectEnds().clear();
        getEnterings().clear();
        getLeavings().clear();
        getDeathRattles().clear();
    }
    public void addStatus(int atk, int hp){
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
        setAtk(finalAtk);
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
        info.msg(getNameWithOwner()+"攻击了对手的"+target.getName()+"！");

        turnAttackOnce();// 总之攻击过一次

        Damage damage = new Damage(this,target);

        if(!getWhenAttacks().isEmpty()) info.msg(getNameWithOwner() + "发动攻击时效果！");
        getWhenAttacks().forEach(whenAttack -> whenAttack.effect().accept(damage));

        if(damage.getTo() instanceof Leader leader)
            leader.damaged(damage);
        if(damage.getTo() instanceof FollowCard toFollow){
            if(!getWhenBattles().isEmpty()){
                info.msg(getNameWithOwner() + "发动交战时效果！");
                getWhenBattles().forEach(whenBattle -> whenBattle.effect().accept(damage));
            }
            if (toFollow.atArea() && !toFollow.getWhenBattles().isEmpty()){
                info.msg(toFollow.getNameWithOwner() + "发动交战时效果！");
                toFollow.getWhenBattles().forEach(whenBattle -> whenBattle.effect().accept(damage));
            }
            if(damage.checkFollowAtArea())
                toFollow.damageBoth(damage);
        }

    }

    public void damageBoth(Damage damage){
        GameObj from = damage.getFrom();
        if(from instanceof FollowCard fromFollow){
            setHp(getHp() - fromFollow.getAtk());
            info.msg(getNameWithOwner()+"受到了来自"+ fromFollow.getName()+"的"+fromFollow.getAtk()+"点伤害" +
                "（剩余"+getHp()+"点生命值）");

            fromFollow.setHp(fromFollow.getHp() - getAtk());
            info.msg(fromFollow.getNameWithOwner()+"受到了来自"+ getName()+"的"+getAtk()+"点反击伤害" +
                "（剩余"+fromFollow.getHp()+"点生命值）");

            this.damageSettlement(new Damage(from,this));
            fromFollow.damageSettlement(new Damage(this,from));
            // 先结算伤害，触发完所有事件后结算武器是否损毁
            if(fromFollow.equipped())
                fromFollow.expireEquipSettlement();
            if(this.equipped())
                this.expireEquipSettlement();
        }else {
            throw new RuntimeException("伤害来源非随从，无法生成反击！");
        }
    }
    public void damagedWithoutSettle(Damage damage){
        if(!damage.isFromAtk() && hasKeyword("效果伤害免疫")){
            damage.setDamage(0);
            info.msg(getNameWithOwner() + "免疫了效果伤害！");
        }
        setHp(getHp()- damage.getDamage());
        info.msg(getNameWithOwner()+"受到了来自"+damage.getFrom().getName()+"的"+damage.getDamage()+"点伤害" +
            "（剩余"+getHp()+"点生命值）");
    }
    public void damageSettlement(Damage damage){

        if(!atArea()) return;
        GameObj from = damage.getFrom();

        // 攻击方是随从，计算攻击方的关键词
        if (from instanceof FollowCard followCard) {
            if(followCard.equipped()) followCard.expireEquip();
            if(followCard.hasKeyword("重伤")){
                info.msg(followCard.getNameWithOwner() + "发动重伤效果！");
                addKeyword("无法回复");
            }
            if(followCard.hasKeyword("自愈")){
                info.msg(followCard.getNameWithOwner() + "发动自愈效果！");
                followCard.heal(damage.getDamage());
            }
            if(followCard.hasKeyword("吸血")){
                info.msg(followCard.getNameWithOwner() + "发动吸血效果！");
                followCard.ownerPlayer().heal(damage.getDamage());
            }
            if(followCard.hasKeyword("剧毒")){
                if(destroyBy(followCard)) return;
            }
        }
        // 计算生命值
        if(getHp() <= 0){
            death();
            if(from instanceof FollowCard fromFollow
                && !fromFollow.getWhenKills().isEmpty()){
                info.msg(fromFollow.getNameWithOwner() + "发动击杀时效果！");
                fromFollow.getWhenKills().forEach(whenKill -> whenKill.effect().accept(this));
            }
        }else {
            if(!getWhenDamageds().isEmpty()){
                info.msg(getNameWithOwner() + "发动受伤时效果！");
                getWhenDamageds().forEach(whenDamaged -> whenDamaged.effect().accept(damage));
            }
        }
    }

    public void damaged(Damage damage){
        damagedWithoutSettle(damage);
        damageSettlement(damage);
    }

    public static class Event {
        /** 攻击时效果 */
        public record WhenAttack(Consumer<Damage> effect){}

        /** 交战时效果 */
        /* 交战时请勿修改双方交战对象 */
        public record WhenBattle(Consumer<Damage> effect){}

        /** 受伤时效果 */
        public record WhenDamaged(Consumer<Damage> effect){}

        /** 激励效果 */
        public record WhenLeaderSkill(FunctionN effect){}

    }
}
