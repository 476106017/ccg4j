package org.example.card.original.disease.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class NarcissisticPersonalityDisorder extends AmuletCard {


   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 1;

    public String name = "自恋型人格障碍";
    public String job = "疾病";
    private List<String> race = Lists.ofStr();

    public String mark = """
        打出随从牌时，该随从发动【自搜 2：获得+2/+2、守护】
        """;
    public String subMark = "";

    public void init() {
        setCountDown(5);
        addEffects(new Effect(this,this, EffectTiming.WhenPlay,
            card-> card instanceof FollowCard, card->{
                    if(card instanceof FollowCard followCard){
                        List<Card> searches = ownerPlayer().getDeck().stream()
                            .filter(card1 -> card1.getName().equals(followCard.getName())).limit(2)
                            .toList();
                        if(searches.size()>=2){
                            ownerPlayer().getDeck().removeAll(searches);
                            followCard.addStatus(2,2);
                            followCard.addKeyword("守护");
                        }
                    }
                }));
    }

}
