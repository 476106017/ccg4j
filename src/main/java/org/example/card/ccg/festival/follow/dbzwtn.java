package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import java.util.function.Consumer;


@Getter
@Setter
public class dbzwtn extends FollowCard {
    public Integer cost = 5;
    public String name = "盗版之王托尼";
    public String job = "中立";
    private List<String> race = Lists.ofStr("海盗");
    public String mark = """
        交换双方牌库 压轴：抽1张牌
        """;
    public String subMark = "";

    public int atk = 4;
    public int hp = 6;

    public dbzwtn() {
        setMaxHp(getHp());

        Consumer<Object> reverseDeck = obj -> {
            List<Card> deck = ownerPlayer().getDeck();
            deck.forEach(GameObj::changeOwner);
            List<Card> enemyDeck = enemyPlayer().getDeck();
            enemyDeck.forEach(GameObj::changeOwner);
            ownerPlayer().setDeck(enemyDeck);
            enemyPlayer().setDeck(deck);
        };
        addEffects((new Effect(this,this, EffectTiming.WhenAtArea, reverseDeck)));
        addEffects((new Effect(this,this, EffectTiming.WhenNoLongerAtArea,reverseDeck)));
        setPlay(new Play(()->{
            if(ownerPlayer().getPpNum()==0){
                ownerPlayer().draw(1);
            }
        }));
    }

}
