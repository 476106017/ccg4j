package org.example.card.fairy.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.fairy.spell.EternalForest;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class EternalBloom extends FollowCard {

    public Integer cost = 0;

    public String name = "永恒之花";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("植物");
    public String mark = """
        瞬念召唤：回合开始时
        离场时：摧毁我方场上所有植物，每摧毁1张，便随机破坏1张对手随从卡，并且抽1张牌
        轮回时：将1张永恒森林洗入牌堆
        突进
        """;
    public String subMark = "";

    public int atk = 0;
    public int hp = 1;

    public EternalBloom() {
        setMaxHp(getHp());
        getKeywords().add("突进");

        getEffects().add(new Effect(this,this, EffectTiming.InvocationBegin,
            ()->true,
            ()->{}
        ));

        getEffects().add(new Effect(this,this, EffectTiming.Leaving,
            ()->{
                List<AreaCard> plants = new ArrayList<>();
                ownerPlayer().getArea().stream()
                    .filter(areaCard -> areaCard.getRace().contains("植物"))
                    .forEach(plants::add);
                plants.forEach(plantCard->{
                    if(plantCard.atArea()){
                        plantCard.death();
                        List<AreaCard> oppositeArea = enemyPlayer().getArea();
                        oppositeArea.get((int) (oppositeArea.size() * Math.random())).death();
                        ownerPlayer().draw(1);
                    }
                });
            }
        ));

        getEffects().add(new Effect(this,this, EffectTiming.Transmigration,
            ()->{
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(EternalForest.class));
                ownerPlayer().addDeck(addCards);
            }
        ));

    }

}
