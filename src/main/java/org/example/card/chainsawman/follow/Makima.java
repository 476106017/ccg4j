package org.example.card.chainsawman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class Makima extends FollowCard {
    private int slot = 7;
    private int apposition = 1;
    private String name = "支配恶魔";
    private Integer cost = 0;
    private int atk = 1;
    private int hp = 1;
    private String job = "链锯人";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        入场时：获得场上所有牌的控制权
        离场时：失去场上所有牌的控制权
        """;
    private String subMark = "";
    public Makima() {
        setMaxHp(getHp());
        getKeywords().add("恶魔转生");
    }
}
