package org.example.card.nemesis.spell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.GameObj;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class CalamitysEnd extends SpellCard {
    public Integer cost = 0;
    public String name = "灾祸降临";
    public String job = "复仇者";
    public String race = "灾祸";
    public String mark = """
        揭示:回合开始时被破坏的5费以上随从大于20个
        破坏双方场上全部随从。
        如果被破坏的5费以上己方随从小于30个，则增加1张灾祸降临到牌堆中；
        如果被破坏的5费以上己方随从大于30个，则赢得胜利
        """;

    public String subMark = "被破坏的5费以上随从：{count}个";

    public String getSubMark() {
        long count = ownerPlayer().getGraveyard().stream()
            .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
            .count();
        return subMark.replaceAll("\\{count}",count+"");
    }


    @Override
    public boolean canInvocationBegin() {
        return ownerPlayer().getGraveyard().stream()
            .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
            .count() >= 20;
    }


    @Override
    public void play(List<GameObj> targets) {
        super.play(targets);

        info.destroy(oppositePlayer().getArea());
        info.destroy(ownerPlayer().getArea());


        long count = ownerPlayer().getGraveyard().stream()
            .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
            .count();

        if(count < 30){
            List<Card> addCards = new ArrayList<>();
            addCards.add(createCard(CalamitysEnd.class));
            info.thisPlayer().addDeck(addCards);
        }else {
            info.gameset(ownerPlayer());
        }


    }
}
