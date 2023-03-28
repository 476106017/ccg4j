package org.example.card.ccg.hunter.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.EquipmentCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;

import java.util.List;

@Getter
@Setter
public class HarpoonGun extends EquipmentCard {
    public Integer cost = 3;
    public String name = "鱼叉炮";
    public transient int addAtk = 3;
    public transient int addHp = 0;
    public String job = "猎人";
    public String mark = """
        攻击时：检查牌堆底部的牌并放到顶部，如果是野兽卡，则费用-2
        """;

    public String subMark = "";

    public HarpoonGun() {
        setCountdown(2);
        setPlay(new Play(()->ownerPlayer().getAreaFollowsAsGameObj()));
        addEffects(new Effect(this,this, EffectTiming.WhenAttack,()->{
            List<Card> deck = ownerPlayer().getDeck();
            if(deck.isEmpty())return;

            Card card = deck.remove(deck.size() - 1);
            deck.add(0, card);
            if(card.hasRace("野兽"))
                card.addCost(-2);
        }));
    }
}