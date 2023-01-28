package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.game.EventType;
import org.example.game.GameObj;


@Getter
@Setter
public abstract class AreaCard extends Card{
    private int leaveIndex = -1;// 离场时所在的下标
    public abstract String getType();

    // 准备破坏
    public GameObj destroyedBy = null;

    public void setDestroyedBy(GameObj destroyedBy) {
        if(info.addEvent(this,EventType.Destroy))
            this.destroyedBy = destroyedBy;
    }

    public void backToHand(){
        if(!atArea())return;

        info.msg(getNameWithOwner() + "返回手牌！");
        if(hasKeyword("魔法免疫")){
            getInfo().msg(getNameWithOwner() + "免疫了本次返回手牌！");
            return;
        }
        if(hasKeyword("魔法护盾")){
            getInfo().msg(getNameWithOwner() + "的魔法护盾抵消了本次返回手牌！");
            removeKeyword("魔法护盾");
            return;
        }


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
    public boolean destroyed(){
        return destroyedBy!=null && destroyedBy(destroyedBy);
    }

    public boolean destroyedBy(GameObj from){
        if(!atArea())return false;

        if(hasKeyword("无法破坏")) {
            info.msg(getNameWithOwner() + "无法破坏！");
            return false;
        }
        if(hasKeyword("魔法免疫")){
            getInfo().msg(getNameWithOwner() + "免疫了本次破坏！");
            return true;
        }
        if(hasKeyword("魔法护盾")){
            getInfo().msg(getNameWithOwner() + "的魔法护盾抵消了本次破坏！");
            removeKeyword("魔法护盾");
            return true;
        }
        info.msg(getNameWithOwner() + "被"+from.getNameWithOwner()+"破坏！");
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

        // 重生时，保留装备，不进墓地，原地重新召唤
        if(hasKeyword("重生")){
            removeKeyword("重生");
            ownerPlayer().summon(this);
            return;
        }

        if(this instanceof FollowCard followCard && followCard.equipped()){
            followCard.getEquipment().death();
        }

        ownerPlayer().addGraveyard(this);
    }
}
