package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;


@Getter
@Setter
public class ccgm extends FollowCard {
    public Integer cost = 1;
    public String name = "吵吵歌迷";
    public String job = "中立";
    private List<String> race = Lists.ofStr("机械");
    public String mark = """
        战吼：选择1个敌方随从，当本随从存活时，该随从被缴械
        """;
    public String subMark = "";

    public int atk = 1;
    public int hp = 2;

    private FollowCard targetFollow;

    public ccgm() {
        setMaxHp(getHp());
        setPlay(new Play(
            ()->enemyPlayer().getAreaFollowsAsGameObj(), false,
            targets->{
                if(targets!=null){
                    final FollowCard followCard = (FollowCard) targets;
                    followCard.addKeyword("缴械");
                    targetFollow = followCard;
                }
            }));

        addEffects((new Effect(this,this, EffectTiming.WhenNoLongerAtArea, obj->{
            if(targetFollow!=null && targetFollow.atArea())targetFollow.removeKeyword("缴械");
        })));
    }

}
