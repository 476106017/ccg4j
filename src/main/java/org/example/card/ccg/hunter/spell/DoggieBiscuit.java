package org.example.card.ccg.hunter.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Getter
@Setter
public class DoggieBiscuit extends SpellCard {
    public Integer cost = 2;
    public String name = "狗狗饼干";
    public String job = "猎人";
    private List<String> race = Lists.ofStr();
    public String mark = """
        选择自己1个场上随从，抉择：
        1. 获得+2/+3；
        2. 获得【突进】，随后主战者+1PP并将1张狗狗饼干洗入牌堆
        """;

    public String subMark = "";

    public DoggieBiscuit() {
        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObj(),2,
            (choice,gameObjs) -> {
                FollowCard followCard = (FollowCard) gameObjs;
                if(choice==1){
                    followCard.addStatus(2,3);
                }else if(choice==2){
                    followCard.addKeyword("突进");
                    ownerPlayer().setPpNum(ownerPlayer().getPpNum()+1);
                    ownerPlayer().addDeck(createCard(DoggieBiscuit.class));
                }
            }));
    }
}
