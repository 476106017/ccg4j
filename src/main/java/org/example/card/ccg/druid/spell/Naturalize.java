package org.example.card.ccg.druid.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Naturalize extends SpellCard {

   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 1;
    public String name = "自然平衡";
    public String job = "德鲁伊";
    private List<String> race = Lists.ofStr("自然");
    public String mark = """
        消灭一个随从，你的对手抽两张牌。
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()->info.getAreaFollowsAsGameObj(),true,
            target->{
                final FollowCard followCard = (FollowCard) target;
                destroy(followCard);
                enemyPlayer().draw(2);
        }));
    }

}
