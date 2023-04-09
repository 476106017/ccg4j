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

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class Metaproduction extends SpellCard {
    public Integer cost = 1;
    public String name = "创造术";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("");
    public String mark = """
        增加1张解析的创造物卡片到牌堆中。
        自己的回合结束时，抽取1张卡片。
        """;

    public String subMark = "";
    public void init() {

        setPlay(new Play(
            ()->{
                ownerPlayer().addDeck(createCard(Yuwan.AnalyzingArtifact.class));
                ownerLeader().addEffect(
                    new Effect(this,null, EffectTiming.EndTurn, 1,
                        obj -> ownerPlayer().draw(1)
                    ), false);
            }));
    }

}
