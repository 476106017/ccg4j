package org.example.card.neutral.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Damage;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Getter
@Setter
public class MysticRing extends SpellCard {
    public Integer cost = 1;
    public String name = "神秘的戒指";
    public String job = "中立";
    private List<String> race = Lists.ofStr("财宝");
    public String mark = """
        返回1张手牌到牌堆
        抽1张牌
        """;

    public String subMark = "";
    public int target = 1;

    @Override
    public void initCounter() {
        this.count();
    }

    public MysticRing() {
        getPlays().add(new Event.Play(()->ownerPlayer().getHandAsGameObjBy(card -> card!=this),
            1,
            targets->{
                Card target = (Card)targets.get(0);
                ownerPlayer().backToDeck(target);
                ownerPlayer().draw(1);
            }
        ));
    }

}
