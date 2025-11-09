package org.example.card.ccg.warlock.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.example.constant.CardRarity;

@Getter
@Setter
public class WickedWhispers extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 1;
    public String name = "邪恶低语";
    public String job = "术士";
    private List<String> race = Lists.ofStr("暗影");
    public String mark = """
        弃掉你手牌中法力值消耗最低的牌。使你的所有随从获得+1/+1。
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            final Optional<Card> first = ownerPlayer().getHand().stream()
                .filter(card -> card!=this)
                .sorted(Comparator.comparingInt(Card::getCost)).findFirst();
            first.ifPresent(card -> {
                ownerPlayer().abandon(card);
                ownerPlayer().getAreaFollowsAsFollow().forEach(followCard -> followCard.addStatus(1,1));
            });
        }));
    }

}
