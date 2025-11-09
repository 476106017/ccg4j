package org.example.card.ccg.rogue.spell;

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
public class EscapePlan extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 1;
    public String name = "逃跑路线";
    public String job = "潜行者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        使1个友方随从获得【亡语：返回手牌】
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(()->ownerPlayer().getAreaFollowsAsGameObj(),
            true,
            target->{
                FollowCard followCard = (FollowCard) target;
                followCard.backToHand();
            }));
    }
}
