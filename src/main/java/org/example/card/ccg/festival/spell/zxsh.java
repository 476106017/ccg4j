package org.example.card.ccg.festival.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class zxsh extends SpellCard {
    public Integer cost = 2;
    public String name = "真弦术：合";
    public String job = "牧师";
    private List<String> race = Lists.ofStr("神圣");
    public String mark = """
        选择一张我方随从，将它的复制置入到手牌。压轴：使本体复制均获得+1/+2
        """;

    public String subMark = "";

    public zxsh() {
        setPlay(new Play(
            () -> ownerPlayer().getAreaFollowsAsGameObj(),true,
            gameObj -> {
                FollowCard followCard = (FollowCard)gameObj;
                FollowCard clone = (FollowCard)followCard.cloneOf(ownerPlayer());
                ownerPlayer().addHand(clone);

                if(ownerPlayer().getPpNum()==0){
                    followCard.addStatus(1,2);
                    clone.addStatus(1,2);
                }
            }));
    }
}
