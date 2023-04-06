package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class K90tron extends FollowCard {
    private String name = "K9-0型机械狗";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 3;
    private String job = "猎人";
    private List<String> race = Lists.ofStr("机械");
    private String mark = """
        战吼：如果底牌是费用为1的随从，则召唤它
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            List<Card> deck = ownerPlayer().getDeck();
            if(!deck.isEmpty()){
                Card card = deck.get(deck.size() - 1);
                if(card instanceof FollowCard followCard && card.getCost()==1){
                    followCard.removeWhenNotAtArea();
                    ownerPlayer().summon(followCard);
                }else {
                    info.msg(getNameWithOwner() + "没有召唤出任何随从！");
                }
            }
        }));
    }

}