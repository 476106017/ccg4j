package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardType;
import org.example.game.PlayerInfo;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public abstract class EquipmentCard extends AreaCard{
    public final CardType TYPE = CardType.EQUIPMENT;
    @Override
    public String getType() {
        return TYPE.getName();
    }

    private List<String> race = Lists.ofStr("装备");

    private FollowCard target;

    private boolean control = false;

    private int addAtk = 0;

    private int addHp = 0;

    private int countdown = -1;// 耐久

    public void addCountdown(int i){
        if(getCountdown() == -1)return;
        setCountdown(getCountdown() + i);
        info.msg(getNameWithOwner()+"可使用次数+"+i+"（还剩"+getCountdown()+"次）");
    }
    public void death(){
        if(target==null)
            info.msg(getNameWithOwner()+"在没有装备任何随从的状态下被破坏了！");
        else{
            // 装备解除前，解除随从身上的装备效果
            if(isControl()){
                if(target.atArea()){
                    info.msg(target.getNameWithOwner() + "解除了控制，移动到"+target.enemyPlayer().getName()+"场上！");
                    target.removeWhenAtArea();
                    // region 转移控制器
                    target.setOwner(1-target.getOwner());
                    target.enemyPlayer().addArea(target);
                    // endregion
                }else {
                    info.msg(target.getNameWithOwner() + "已经被彻底控制了......");
                }
                target.removeKeyword("被控制");
            }
            target.removeKeywords(getKeywords());
            target.addStatus(-getAddAtk(),-getAddHp());

            info.msg(target.getNameWithOwner()+"的装备"+getName()+"被破坏了！");
            target.setEquipment(null);
            setTarget(null);
        }

        if(!getWhenNoLongerAtAreas().isEmpty()) {
            info.msg(getNameWithOwner() + "在场时效果消失！");
            getWhenNoLongerAtAreas().forEach(noLongerAtArea -> noLongerAtArea.effect().apply());
        }
        if(!getLeavings().isEmpty()){
            info.msg(getNameWithOwner() + "发动离场时效果！");
            getLeavings().forEach(leaving -> leaving.effect().apply());
        }
        if(!getDeathRattles().isEmpty()){
            info.msg(getNameWithOwner() + "发动亡语效果！");
            getDeathRattles().forEach(leaving -> leaving.effect().apply());
        }
        ownerPlayer().getGraveyard().add(this);
        ownerPlayer().countToGraveyard(1);

    }

}
