package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Peasant extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "农夫";
    private Integer cost = 1;
    private int atk = 2;
    private int hp = 1;
    private String job = "中立";
    private List<String> race = Lists.ofStr();
    private String mark = """
        回合开始时：抽1张牌
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects(new Effect(this,this, EffectTiming.BeginTurn,
            ()->ownerPlayer().draw(1)));
    }
}
