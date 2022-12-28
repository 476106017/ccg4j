package org.example.card.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class CalamitysEnd extends SpellCard {
    public Integer cost = 0;
    public String name = "灾祸降临";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("终极灾厄");
    public String mark = """
        揭示:回合开始时被破坏的5费以上随从大于20个
        破坏双方场上全部随从。
        如果被破坏的5费以上己方随从小于30个，则将1张灾祸降临洗入牌堆；
        如果被破坏的5费以上己方随从大于30个，则赢得胜利
        """;

    public String subMark = "被破坏的5费以上随从：{count}个";

    public String getSubMark() {
        long count = ownerPlayer().getGraveyard().stream()
            .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
            .count();
        return subMark.replaceAll("\\{count}",count+"");
    }

    public CalamitysEnd() {

        getPlays().add(new Card.Event.Play(ArrayList::new,0,
            gameObjs -> {
                info.destroy(enemyPlayer().getArea());
                info.destroy(ownerPlayer().getArea());

                long count = ownerPlayer().getGraveyard().stream()
                    .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                    .count();
                if(count < 30){
                    List<Card> addCards = new ArrayList<>();
                    addCards.add(createCard(CalamitysEnd.class));
                    ownerPlayer().addDeck(addCards);
                }else {
                    info.gameset(ownerPlayer());
                }
            }
        ));
        getInvocationBegins().add(new Card.Event.InvocationBegin(
            ()-> ownerPlayer().getGraveyard().stream()
                .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                .count() >= 20,
            ()->{}
        ));
    }


}
