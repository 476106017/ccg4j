package org.example.morecard.xie.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.other.test.folow.TestFollow;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Test02 extends FollowCard {
    private String name = "Test02";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 3;
    private String job = "谢test";
    private List<String> race = Lists.ofStr();
    private String mark = """
        """;
    private String subMark = "";


    public void init() {
        setMaxHp(getHp());
    }
}