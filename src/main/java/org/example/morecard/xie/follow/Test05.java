package org.example.morecard.xie.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class Test05 extends FollowCard {
    private String name = "Test05";
    private Integer cost = 2;
    private int atk = 1;
    private int hp = 1;
    private String job = "谢test";
    private List<String> race = Lists.ofStr();
    private String mark = """
        """;
    private String subMark = "";


    public void init() {
        setMaxHp(getHp());
        getKeywords().add("格挡");
        getKeywords().add("格挡");
    }
}