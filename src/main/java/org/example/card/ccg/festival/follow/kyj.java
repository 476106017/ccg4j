package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class kyj extends FollowCard {
    public Integer cost = 2;
    public String name = "扩音机";
    public String job = "中立";
    private List<String> race = Lists.ofStr("机械");
    public String mark = """
        战吼：将你的法力值上限和手牌上限变为11
        """;
    public String subMark = "";

    public int atk = 2;
    public int hp = 3;


    public kyj() {
        setMaxHp(getHp());

        setPlay(new Play(()->{
            ownerPlayer().setPpLimit(11);
            if(ownerPlayer().getPpMax()>11)
                ownerPlayer().setPpMax(11);

            ownerPlayer().setHandMax(11);
        }));
    }

}
