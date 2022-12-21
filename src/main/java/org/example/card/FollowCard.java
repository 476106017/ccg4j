package org.example.card;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.constant.CardType;
import org.example.game.GameObj;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class FollowCard extends AreaCard{
    public final CardType TYPE = CardType.FOLLOW;
    public int atk = 0;
    public int hp = 0;
    public int maxHp = 0;
    public int turnAge = 0;
    public int turnAttackMax = 1;
    public int turnAttack = 0;

    @Override
    public String getType() {
        return TYPE.getName();
    }

    public void turnAttackOnce(){
        setTurnAttack(getTurnAttack()+1);
    }

    public boolean damaged(int damage){
        if(!ownerPlayer().getArea().contains(this)){
            // 可能被攻击对象亡语效果击杀了本卡，此时不再计算伤害
            info.msg(getNameWithOwner()+"已退场，忽略该伤害");
            return false;
        }
        info.msg(getNameWithOwner()+"受到了"+damage+"点伤害");
        if(hp>damage){
            hp -= damage;
            return false;
        }else {
            hp = 0;
            death();
            return true;
        }
    }
}
