package org.example.card.ccg.mage.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;
import org.example.constant.CardRarity;

@Getter
@Setter
public class WandThief extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "魔杖窃贼";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "法师";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：如果本回合使用的卡牌数高于1,则发现1张法术牌
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            if(ownerPlayer().getCount(PLAY_NUM)>0){
                ownerPlayer().discoverCard(card -> card instanceof SpellCard,
                    prototype-> ownerPlayer().addHand(prototype.copyBy(ownerPlayer())));
            }
        }));

    }
}
