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
import org.example.constant.CardRarity;


@Getter
@Setter
public class Scavenge extends SpellCard {

   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 2;
    public String name = "废品的拣选";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("");
    public String mark = """
        从墓地搜索费用为1、3、5、7、9的随从，各取1张置入手牌。
        """;

    public String subMark = "";
    public void init() {

        setPlay(new Play(
            ()->{
                final List<Card> cost1List = ownerPlayer().getGraveyardBy(p -> p.getCost() == 1);
                if(cost1List.size()>0){
                    final Card cost1 = Lists.randOf(cost1List);
                    cost1.removeWhenNotAtArea();
                    ownerPlayer().addHand(cost1);
                }

                final List<Card> cost3List = ownerPlayer().getGraveyardBy(p -> p.getCost() == 3);
                if(cost3List.size()>0){
                    final Card cost1 = Lists.randOf(cost3List);
                    cost1.removeWhenNotAtArea();
                    ownerPlayer().addHand(cost1);
                }

                final List<Card> cost5List = ownerPlayer().getGraveyardBy(p -> p.getCost() == 5);
                if(cost5List.size()>0){
                    final Card cost1 = Lists.randOf(cost5List);
                    cost1.removeWhenNotAtArea();
                    ownerPlayer().addHand(cost1);
                }

                final List<Card> cost7List = ownerPlayer().getGraveyardBy(p -> p.getCost() == 7);
                if(cost7List.size()>0){
                    final Card cost1 = Lists.randOf(cost7List);
                    cost1.removeWhenNotAtArea();
                    ownerPlayer().addHand(cost1);
                }

                final List<Card> cost9List = ownerPlayer().getGraveyardBy(p -> p.getCost() == 9);
                if(cost9List.size()>0){
                    final Card cost1 = Lists.randOf(cost9List);
                    cost1.removeWhenNotAtArea();
                    ownerPlayer().addHand(cost1);
                }

            }));
    }

}
