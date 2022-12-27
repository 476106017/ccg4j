package org.example.card;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.constant.CardType;
import org.example.game.Damage;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.system.function.FunctionN;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class FollowCard extends AreaCard{
    public final CardType TYPE = CardType.FOLLOW;
    private int atk = 0;
    private int hp = 0;
    private int maxHp = 0;
    private int turnAge = 0;
    private int turnAttackMax = 1;
    private int turnAttack = 0;

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

    public FollowCard() {
        // 实例化随从牌时，需要区分血量和血上限
        setMaxHp(getHp());
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
            if(!getWhenBattles().isEmpty())
                info.msg(getNameWithOwner() + "发动交战时效果！");
            getWhenBattles().forEach(whenBattle -> whenBattle.effect().accept(damage));

            if (!toFollow.getWhenBattles().isEmpty())
                info.msg(toFollow.getNameWithOwner() + "发动交战时效果！");
            toFollow.getWhenBattles().forEach(whenBattle -> whenBattle.effect().accept(damage));

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

            damageSettlement(new Damage(from,this));
            damageSettlement(new Damage(this,from));
        }else {
            throw new RuntimeException("伤害来源非随从，无法生成反击！");
        }
    }
    public void damagedWithoutSettle(Damage damage){
        if(!damage.isFromAtk() && hasKeyword("效果伤害免疫")){
            damage.setDamage(0);
            info.msg(getNameWithOwner() + "免疫了效果伤害！");
        }
        info.msg(getNameWithOwner()+"受到了来自"+damage.getFrom().getName()+"的"+damage.getDamage()+"点伤害" +
            "（剩余"+getHp()+"点生命值）");
        setHp(getHp()- damage.getDamage());
    }
    public boolean damageSettlement(Damage damage){

        if(!damage.checkFollowAtArea()) return false;
        GameObj from = damage.getFrom();
        if(from instanceof Card card && card.hasKeyword("吸血")){
            info.msg(card.getNameWithOwner() + "发动吸血效果！");
            card.ownerPlayer().heal(damage.getDamage());
        }
        if(!getWhenDamageds().isEmpty()){
            info.msg(getNameWithOwner() + "发动受伤时效果！");
            getWhenDamageds().forEach(whenDamaged -> whenDamaged.effect().accept(damage));
        }
        if(!damage.checkFollowAtArea()) return false;// 因受伤时效果自灭了，不算击杀

        // 由剧毒随从攻击
        if(from instanceof FollowCard fromFollow && fromFollow.hasKeyword("剧毒") ){
            if(destroyBy(fromFollow)) return true;
        }
        // 未被效果破坏，计算生命值
        if(getHp() > 0){
            return false;
        }else {
            death();
            if(from instanceof FollowCard fromFollow
                && fromFollow.atArea() // 受伤时效果发动后【攻击者】还在场
                && !fromFollow.getWhenKills().isEmpty()){
                info.msg(fromFollow.getNameWithOwner() + "发动击杀时效果！");
                fromFollow.getWhenKills().forEach(whenKill -> whenKill.effect().accept(this));
            }
            return true;
        }

    }

    public boolean damaged(Damage damage){
        damagedWithoutSettle(damage);
        return damageSettlement(damage);
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
