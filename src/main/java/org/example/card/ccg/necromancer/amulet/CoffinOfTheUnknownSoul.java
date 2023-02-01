package org.example.card.ccg.necromancer.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class CoffinOfTheUnknownSoul extends AmuletCard {

    public Integer cost = 2;

    public String name = "幽魂之棺";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public int countDown = 1;

    public String mark = """
        战吼：如果葬送发动，则抽1张牌，且使该护符倒数增加被葬送卡费用的一半
        亡语：召还葬送的随从
        """;
    public String subMark = "";

    public FollowCard followCard;

    public CoffinOfTheUnknownSoul() {
        setPlay(new Play(()->ownerPlayer().getHandAsGameObjBy(card -> card instanceof FollowCard),
            false,
            obj->{
                if(obj==null || !ownerPlayer().burial((FollowCard) obj))return;
                followCard = (FollowCard)obj;
                ownerPlayer().draw(1);
                countDown += followCard.getCost()/2;
            }));
        addEffects((new Effect(this,this,
            EffectTiming.DeathRattle, obj -> ownerPlayer().recall(followCard))));
    }

}
