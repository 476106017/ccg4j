package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class sysfze extends FollowCard {
    public Integer cost = 3;
    public String name = "摄影师菲兹尔";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        战吼：拍摄你的手牌，并将照片洗入牌库
        压轴：然后自拍一张！
        """;
    public String subMark = "";

    public int atk = 3;
    public int hp = 3;

    private FollowCard targetFollow;

    public sysfze() {
        setMaxHp(getHp());

        setPlay(new Play(()->{
            List<Card> copy = new ArrayList<>();
            ownerPlayer().getHand().forEach(card -> copy.add(card.cloneOf(ownerPlayer())));
            ownerPlayer().addDeck(copy);
            if(ownerPlayer().getPpNum()==0){
                ownerPlayer().addDeck(cloneOf(ownerPlayer()));
            }
        }));
    }

}
