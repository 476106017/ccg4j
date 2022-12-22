package org.example.card.nemesis.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class MagisterialDreadnought extends FollowCard {
    private Integer cost = 5;
    private String name = "正义暴君";
    private String job = "复仇者";
    private String race = "";
    private String mark = """
        瞬念召唤：回合开始时被破坏的5费以上随从大于5个
        入场时：召唤1个世界驱除者
        离场时：增加2张幻境粉碎者到牌堆中
        """;
    private String subMark = "";

    private int atk = 5;
    private int hp = 5;
    private int maxHp = 5;

    @Override
    public void entering() {
        info.msg(getName() + "发动入场时效果！");
        ownerPlayer().summon(createCard(WorldEliminator.class));
    }

    @Override
    public void leaving() {
        List<Card> addCards = new ArrayList<>();
        addCards.add(createCard(RuinerOfEden.class));
        addCards.add(createCard(RuinerOfEden.class));
        ownerPlayer().addDeck(addCards);
    }

    @Override
    public boolean canInvocationBegin() {
        return ownerPlayer().getGraveyard().stream()
            .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
            .count() >= 5;
    }
}
