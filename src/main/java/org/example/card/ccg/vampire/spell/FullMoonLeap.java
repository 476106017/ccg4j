package org.example.card.ccg.vampire.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class FullMoonLeap extends SpellCard {
    public Integer cost = 2;
    public String name = "月下纵身";
    public String job = "吸血鬼";
    private List<String> race = Lists.ofStr();
    public String mark = """
        给予1个自己的吸血鬼从者+2/+0效果。
        如果自己手牌中的卡片张数为2以下（不包含本卡片），则会给予该从者疾驰效果。
        """;

    public String subMark = "";


    public FullMoonLeap() {
        setPlay(new Play(()-> ownerPlayer().getAreaFollowsAsGameObjBy(p->p.getJob().equals("吸血鬼")),
            true,
            target->{
                FollowCard followCard = (FollowCard) target;
                followCard.addStatus(2,0);
                if(ownerPlayer().getHand().size()<=2){
                    followCard.addKeyword("疾驰");
                }
            }));
    }

}
