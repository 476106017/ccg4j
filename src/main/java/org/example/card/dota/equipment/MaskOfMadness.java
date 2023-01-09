package org.example.card.dota.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.EquipmentCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;

import java.util.List;

@Getter
@Setter
public class MaskOfMadness extends EquipmentCard {
    public Integer cost = 2;
    public String name = "疯狂面具";
    public int addAtk = 0;
    public int addHp = 0;
    public String job = "dota";
    public String mark = """
        装备对象受到2倍的伤害
        """;

    public String subMark = "";

    public MaskOfMadness() {
        getKeywords().add("自愈");
        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObj(),true,
            gameObj -> {
                gameObj.addEffects(new Effect(this,gameObj,EffectTiming.AfterDamaged,obj->{
                    Damage damage = (Damage) obj;
                    damage.setDamage(damage.getDamage() * 2);
                }));
            }));
    }
}