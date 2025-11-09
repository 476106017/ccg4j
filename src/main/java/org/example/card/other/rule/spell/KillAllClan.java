package org.example.card.other.rule.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class KillAllClan extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 9;
    public String name = "诛连九族";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr();
    public String mark = """
        破坏敌方场上全部随从，
        破坏创造他们的全部随从；破坏他们创造的全部随从；
        （如果在场上则破坏、在手牌则舍弃、在牌堆则移至墓地）
        """;

    public String subMark = "";
    public int target = 2;

    public void init() {
        setPlay(new Play(()->{
            List<AreaCard> follows = enemyPlayer().getAreaFollows();
            destroy(follows);

            follows.stream()
                .filter(areaCard -> areaCard.getParent() instanceof FollowCard)
                .forEach(areaCard -> {
                    if(areaCard.atArea()) destroy(areaCard);
                    if(areaCard.atHand()) ownerPlayer().abandon(areaCard);
                    if(areaCard.atDeck()) {
                        areaCard.removeWhenNotAtArea();
                        ownerPlayer().addGraveyard(areaCard);
                    }
                });

            ownerPlayer().getAreaCopy().forEach(areaCard -> {
                if(follows.contains(areaCard.getParent())){
                    destroy(areaCard);
                }
            });
            ownerPlayer().getHandCopy().forEach(card -> {
                if(follows.contains(card.getParent())){
                    ownerPlayer().abandon(card);
                }
            });
            ownerPlayer().getDeckCopy().forEach(card -> {
                if(follows.contains(card.getParent())){
                    card.removeWhenNotAtArea();
                    ownerPlayer().addGraveyard(card);
                }
            });
        }));
    }

}
