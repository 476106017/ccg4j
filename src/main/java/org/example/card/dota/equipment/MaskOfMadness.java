package org.example.card.dota.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.constant.CardRarity;

@Getter
@Setter
public class MaskOfMadness extends EquipmentCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 2;
    public String name = "疯狂面具";
    public transient int addAtk = 0;
    public transient int addHp = 0;
    public String job = "dota";
    public String mark = """
        战吼：装备对象受到2倍的伤害
        """;

    public String subMark = "";

    public void init() {
        getKeywords().add("自愈");
        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObj(),true,
            gameObj -> {
                gameObj.addEffects(new Effect(this,gameObj,EffectTiming.AfterDamaged,obj->{
                    Damage damage = (Damage) obj;
                    damage.setDamage(damage.getDamage() * 2);
                    info.msg(damage.getTo().getName() + "遭受的伤害变成了2倍（"+damage.getDamage()+"点）！");
                }));
            }));
    }
}
