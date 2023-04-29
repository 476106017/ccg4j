package org.example.card.ccg.sts.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class MasterReality extends AmuletCard {
    public Integer cost = 2;
    public String name = "操控现实";
    public String job = "杀戮尖塔";
    private List<String> race = Lists.ofStr();
    public String mark = """
        所有临时卡片都会升级（随从则会进化）
        """;

    public String subMark = "";

    public void init() {
        addEffects(new Effect(this,this, EffectTiming.WhenCreateCard,
            obj->{
                Card card = (Card)obj;
                card.upgrade();
                if(card instanceof FollowCard followCard){
                    followCard.addStatus(2,2);
                    followCard.addKeyword("突进");
                }
            }));
    }
}
