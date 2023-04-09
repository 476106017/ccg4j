package org.example.morecard.xie.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Test22 extends FollowCard {
    private String name = "Test22";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 2;
    private String job = "谢test";
    private List<String> race = Lists.ofStr();
    private String mark = """
        """;
    private String subMark = "";


    public void init() {
        setMaxHp(getHp());
        getKeywords().add("穿透");
    }
}