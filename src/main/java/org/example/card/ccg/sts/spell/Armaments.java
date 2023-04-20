package org.example.card.ccg.sts.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.BLOCK;

@Getter
@Setter
public class Armaments extends SpellCard {
    public Integer cost = 2;
    public String name = "武装";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        获得5点格挡。
        升级手牌中的1张(和所有)牌
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()-> ownerPlayer().getHandAsGameObj(),
            true,
            target->{
                ownerPlayer().count(BLOCK,5);
                if(isUpgrade()){
                    ownerPlayer().getHand().forEach(Card::upgrade);
                }else {
                    ((Card)target).upgrade();
                }
            }
        ));
    }

}
