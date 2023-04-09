package org.example.morecard.xie.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Test06 extends FollowCard {
    private String name = "Test06";
    private Integer cost = 3;
    private int atk = 3;
    private int hp = 4;
    private String job = "谢test";
    private List<String> race = Lists.ofStr();
    private String mark = """
        """;
    private String subMark = "";


    public void init() {
        setMaxHp(getHp());
    }
}