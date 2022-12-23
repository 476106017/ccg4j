package org.example.card;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.constant.CardType;
import org.example.game.GameObj;
import org.example.system.function.FunctionN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class FollowCard extends AreaCard{
    public final CardType TYPE = CardType.FOLLOW;
    public int atk = 0;
    public int hp = 0;
    public int maxHp = 0;
    public int turnAge = 0;
    public boolean isDash = false;
    public int turnAttackMax = 1;
    public int turnAttack = 0;

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

    public void acquireDash(){
        info.msg(getNameWithOwner()+"获得了【突进】");
        this.setDash(true);
    }

    public void changeStatus(int atk,int hp){
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

    public void attack(FollowCard target){
        info.msg(getNameWithOwner()+"攻击了对手的"+target.getName()+"！");

        // region 攻击前，触发来源随从攻击时、两只随从交战时
        if(!getWhenAttacks().isEmpty()) info.msg(getNameWithOwner() + "发动攻击时效果！");
        getWhenAttacks().forEach(whenAttack -> whenAttack.effect().accept(target));

        if(atArea() && target.atArea()){
            if(!getWhenBattles().isEmpty()) info.msg(getNameWithOwner() + "发动交战时效果！");
            getWhenBattles().forEach(whenBattle -> whenBattle.effect().accept(target));
            if(!target.getWhenBattles().isEmpty()) info.msg(target.getNameWithOwner() + "发动交战时效果！");
            target.getWhenBattles().forEach(whenBattle -> whenBattle.effect().accept(this));
        }
        // endregion

        if(atArea() && target.atArea()){
            turnAttackOnce();
            target.damaged(this, getAtk());
        }

        if(atArea() && target.atArea()) {
            info.msg(target.getNameWithOwner() + "反击！");
            damaged(target, target.getAtk());
        }

    }
    public boolean damaged(Card from, int damage){
        info.msg(getNameWithOwner()+"受到了来自"+from.getName()+"的"+damage+"点伤害");
        AtomicInteger damageValue = new AtomicInteger(damage);

        if(!getWhenDamageds().isEmpty()){
            info.msg(getNameWithOwner() + "发动受伤时效果！");
        }
        getWhenDamageds().forEach(whenDamaged -> whenDamaged.effect().accept(from,damageValue));


        if(getHp() > damageValue.get()){
            setHp(getHp()- damageValue.get());
            return false;
        }else {
            setHp(0);
            death();
            if(from.getWhenKills().isEmpty()){
                info.msg(getNameWithOwner() + "发动击杀时效果！");
            }
            from.getWhenKills().forEach(whenKill -> whenKill.effect().accept(this));
            return true;
        }
    }

    public static class Event {
        /** 攻击时效果 */
        public record WhenAttack(Consumer<FollowCard> effect){}
        /** 交战时效果 */
        public record WhenBattle(Consumer<FollowCard> effect){}
        /** 受伤时效果 */
        public record WhenDamaged(BiConsumer<Card,AtomicInteger> effect){}
        /** 激励效果 */
        public record WhenLeaderSkill(FunctionN effect){}

    }
}
