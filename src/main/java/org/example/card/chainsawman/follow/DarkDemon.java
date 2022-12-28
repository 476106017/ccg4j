package org.example.card.chainsawman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class DarkDemon extends FollowCard {
    private int slot = 7;
    private int apposition = 1;
    private String name = "暗之恶魔";
    private Integer cost = 4;
    private int atk = 4;
    private int hp = 4;
    private String job = "链锯人";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        亡语：主战者获得效果【己方回合开始时，如果暗之恶魔在己方墓地，则吸收对方墓地】
        """;
    private String subMark = "";
    public DarkDemon() {
        setMaxHp(getHp());
        getKeywords().add("恶魔转生");
    }
}
