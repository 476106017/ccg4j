package org.example.card.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.fairy.follow.Fairy;
import org.example.game.GameObj;
import org.example.system.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;


@Getter
@Setter
public class SylvanJustice extends SpellCard {
    public Integer cost = 2;
    public String name = "森林的反扑";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        对敌方场上一名随从造成3点伤害，增加1张妖精到手牌
        """;

    public String subMark = "";

    public SylvanJustice() {
        getPlays().add(new Card.Event.Play(
            ()->enemyPlayer().getAreaFollowsAsGameObj(), 1,
            targets->{
                FollowCard followCard = (FollowCard) targets.get(0);
                followCard.damaged(this,3);
                ownerPlayer().addHand(createCard(Fairy.class));
            }));
    }
}
