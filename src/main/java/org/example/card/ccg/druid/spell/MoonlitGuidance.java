package org.example.card.ccg.druid.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class MoonlitGuidance extends SpellCard {

   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 2;
    public String name = "月光指引";
    public String job = "德鲁伊";
    private List<String> race = Lists.ofStr("奥术");
    public String mark = """
        发现你牌库中一张牌的复制。如果你在本回合中使用这张复制，则抽取本体。
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()->{
            ownerPlayer().discoverCard(ownerPlayer().getDeck(),discoverCard ->{
                final Card copy = discoverCard.copyBy(ownerPlayer());
                ownerPlayer().addHand(copy);
                ownerLeader().addEffect(new Effect(this, ownerLeader(), EffectTiming.WhenPlay,1,
                    target -> target == copy,// 打出这张
                    target -> ownerPlayer().draw(card -> card==discoverCard)),// 抽到本体
                    true);
            });
        }));
    }

}
