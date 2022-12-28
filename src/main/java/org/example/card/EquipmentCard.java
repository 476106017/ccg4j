package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardType;
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

    private String targetName;

    private FollowCard target;

    private int addAtk = 0;

    private int addHp = 0;

    private int countdown = -1;// 可使用次数

    public void addCountdown(int i){
        if(getCountdown() == -1)return;
        info.msg(getNameWithOwner()+"可使用次数+"+i+"（还剩"+getCountdown()+"次）");
        setCountdown(getCountdown() + i);
    }
    public void death(){
        info.msg(target.getNameWithOwner()+"已经解除装备"+getName()+"！");
        target.setEquipment(null);
        target.removeKeywords(getKeywords());
        target.addStatus(-getAddAtk(),-getAddHp());
        setTarget(null);

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
