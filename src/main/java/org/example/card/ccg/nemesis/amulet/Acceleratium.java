package org.example.card.ccg.nemesis.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;
import org.example.system.util.MyMath;

import java.util.List;

@Getter
@Setter
public class Acceleratium extends AmuletCard {
    public Integer cost = 1;
    public String name = "加速装置";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        倒数 3
        召唤时，创造物随从：使其获得【突进】。
        使用卡牌时，创造物卡：回复自己的PP 1点。
        """;

    public String subMark = "";

    public void init() {
        setCountDown(3);
        addEffects((new Effect(this,this,
            EffectTiming.WhenSummon,obj-> {
                final List<AreaCard> areaCards = (List<AreaCard>) obj;
                return areaCards.stream().anyMatch(p -> p instanceof FollowCard && p.hasRace("创造物"));
            },
            obj->{
                final List<AreaCard> areaCards = (List<AreaCard>)obj;
                areaCards.stream().filter(p -> p.hasRace("创造物"))
                    .forEach(p->p.addKeyword("突进"));
            })));

        addEffects((new Effect(this,this, EffectTiming.WhenPlay,
            obj-> ((Card) obj).hasRace("创造物"),
            obj-> ownerPlayer().addPp(1))));
    }
}
