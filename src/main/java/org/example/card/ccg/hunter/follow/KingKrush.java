package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class KingKrush extends FollowCard {
    private String name = "暴龙王克鲁什";
    private Integer cost = 9;
    private int atk = 8;
    private int hp = 8;
    private String job = "猎人";
    private List<String> race = Lists.ofStr("野兽");
    private String mark = "";

    public String subMark = "";


    public KingKrush() {
        setMaxHp(getHp());
        getKeywords().add("疾驰");
    }

}