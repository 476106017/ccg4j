package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class rcclz extends FollowCard {
    public Integer cost = 1;
    public String name = "人潮冲浪者";
    public String job = "中立";
    private List<String> race = Lists.ofStr("鱼人");
    public String mark = """
        亡语：使任意一个其他随从获得+1/+1和此亡语
        """;
    public String subMark = "";

    public int atk = 1;
    public int hp = 1;

    public rcclz() {
        setMaxHp(getHp());
        // TODO
    }

}
