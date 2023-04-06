package org.example.card.paripi.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class PartyServant extends FollowCard {
    private String name = "明星侍从";
    private Integer cost = 4;
    private int atk = 1;
    private int hp = 1;
    private String job = "派对咖";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：抽2张牌，如果抽到战吼随从，则召唤到场上并且触发战吼效果
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            List<Card> draw = ownerPlayer().draw(2);
            draw.forEach(card ->{
                if(card instanceof FollowCard followCard && followCard.getPlay()!=null){
                    ownerPlayer().summon(followCard);
                    followCard.fanfare();
                }
            });

        }));

    }
}