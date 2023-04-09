package org.example.card.ccg.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.card.ccg.nemesis.Yuwan;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class AugmentationBestowal extends SpellCard {
    public Integer cost = 1;
    public String name = "机械的解放";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("");
    public String mark = """
        直至回合结束，我方主战者获得
        【召唤时：如果召唤的是创造物卡，回复自己的PP 1点，并抽取1张卡片。】
        """;

    public String subMark = "";
    public void init() {

        setPlay(new Play(
            ()->{
                ownerLeader().addEffect(
                    new Effect(this,null, EffectTiming.WhenSummon, 1,
                        obj -> {
                            final List<Card> cards = (List<Card>) obj;
                            return cards.stream().anyMatch(p->p.hasRace("创造物"));
                        },
                        obj -> {
                            final List<Card> cards = (List<Card>) obj;
                            cards.stream().filter(p->p.hasRace("创造物")).forEach(p->{
                                ownerPlayer().addPp(1);
                                ownerPlayer().draw(1);
                            });
                        }
                    ), false);
            }));
    }

}
