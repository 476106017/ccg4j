package org.example.card.ccg.necromancer.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.AreaCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class DiedParty extends AmuletCard {

    public Integer cost = 1;

    public String name = "坟头蹦迪";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();

    public String mark = """
        其他卡牌召还时：召还墓地里和该卡牌同名的全部随从
        """;
    public String subMark = "";

    public void init() {
        setCountDown(3);
        addEffects((new Effect(this,this, EffectTiming.WhenOthersRecall,
            obj-> {
                if(ownerPlayer().getArea().size() >= ownerPlayer().getAreaMax())
                    return false;

                List<AreaCard> list = (List<AreaCard>) obj;
                List<String> names = list.stream().map(AreaCard::getName).toList();
                return ownerPlayer().getGraveyard().stream()
                    .anyMatch(card -> names.contains(card.getName()));
            },
            obj-> {
                List<AreaCard> list = (List<AreaCard>) obj;
                List<String> names = list.stream().map(AreaCard::getName).toList();
                List<AreaCard> areaCards = ownerPlayer().getGraveyardCopy().stream()
                    .filter(card -> card instanceof AreaCard && names.contains(card.getName()))
                    .map(card -> (AreaCard) card).toList();
                ownerPlayer().recall(areaCards);
            })));
    }

}
