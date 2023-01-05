package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.game.GameObj;


@Getter
@Setter
public abstract class AreaCard extends Card{
    public abstract String getType();

    // 准备破坏
    public GameObj destroyedBy = null;

    public void backToHand(){
        if(!atArea())return;

        info.msg(getNameWithOwner() + "返回手牌！");


        removeWhenAtArea();
        tempEffects(EffectTiming.Leaving);


        if (this instanceof FollowCard followCard){
            // 随从回去，装备破坏
            if (followCard.equipped()) {
                info.msg(getNameWithOwner() + "的装备被留在了战场！");
                followCard.getEquipment().death();
            }
            followCard.setHp(followCard.getMaxHp());// 回复到满血
        }else if (this instanceof AmuletCard amuletCard){
            amuletCard.setCountDown(((AmuletCard)(amuletCard.prototype())).getCountDown());
        }

        ownerPlayer().addHand(this);

        tempEffects(EffectTiming.WhenBackToHand);
    }

    public boolean destroyedBy(GameObj from){
        if(!atArea())return false;

        if(hasKeyword("无法破坏")) {
            info.msg(getIdWithOwner() + "无法破坏！");
            return false;
        }
        info.msg(getIdWithOwner() + "被"+from.getIdWithOwner()+"破坏！");
        death();
        if(this instanceof FollowCard thisFollow && from instanceof Card card){
            card.tempEffects(EffectTiming.WhenKill,thisFollow);
        }
        return true;
    }

    public void death(){
        if(!atArea())return;
        info.msg(getNameWithOwner()+"被送入墓地！");

        if(hasKeyword("游魂")){
            info.msg("墓地拒绝了【游魂】！");
            info.exile(this);
            return;
        }

        // 注能
        ownerPlayer().getHand().forEach(card ->card.tempEffects(EffectTiming.Charge,this));

        removeWhenAtArea();
        tempEffects(EffectTiming.Leaving);
        tempEffects(EffectTiming.DeathRattle);

        if(this instanceof FollowCard followCard && followCard.equipped()){
            followCard.getEquipment().death();
        }

        ownerPlayer().getGraveyard().add(this);
        ownerPlayer().countToGraveyard(1);
    }
}
