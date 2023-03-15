package org.example.card.ccg.fairy.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Getter
@Setter
public class ArborealCore extends AmuletCard {

    public Integer cost = 3;

    public String name = "森林之核";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("机械");
    public String mark = """
        我方召唤时：除外此卡，增加2张妖精萤火到手牌中。
        """;
    public String subMark = "";

    public ArborealCore() {
        addEffects((new Effect(this,this, EffectTiming.WhenSummon, obj->{
            getInfo().exile(this);

            List<Card> addCards = new ArrayList<>();
            addCards.add(createCard(Derivant.FairyWisp.class));
            addCards.add(createCard(Derivant.FairyWisp.class));
            ownerPlayer().addHand(addCards);
        })));
    }

}
