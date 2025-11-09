package org.example.card.original.disease.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class LonelyPatient extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "孤独患者";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "疾病";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：舍弃你的手牌
        每当抽到牌，就舍弃
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(
            ()-> ownerPlayer().abandon(ownerPlayer().getHandCopy())));
        addEffects(new Effect(this,this, EffectTiming.WhenDraw, obj->{
            List<Card> cards = (List<Card>) obj;
            ownerPlayer().abandon(cards);
        }));
    }
}
