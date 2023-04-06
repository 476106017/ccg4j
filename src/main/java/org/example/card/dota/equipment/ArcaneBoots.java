package org.example.card.dota.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;

@Getter
@Setter
public class ArcaneBoots extends EquipmentCard {
    public Integer cost = 3;
    public String name = "秘法鞋";
    public transient int addAtk = 0;
    public transient int addHp = 0;
    public String job = "dota";
    public String mark = """
        战吼：PP最大值+1
        回合开始时：PP+1
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(()->ownerPlayer().getAreaFollowsAsGameObj(),true,obj -> {
            int ppMax = ownerPlayer().getPpMax();
            ownerPlayer().setPpMax(ppMax + 1);
        }));
        addEffects(new Effect(this,this, EffectTiming.BeginTurn,()->{
            int ppNum = ownerPlayer().getPpNum();
            ownerPlayer().setPpNum(ppNum + 1);
        }));
    }
}