package org.example.card.ccg.fairy.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;


@Getter
@Setter
public class Rhinoceroach extends FollowCard {
    public Integer cost = 2;
    public String name = "破魔虫";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("昆虫");
    public String mark = """
        战吼：+X/+0(X是本回合使用的卡牌数)
        """;

    public String subMark = "X等于{}";
    public String getSubMark() {
        return subMark.replaceAll("\\{}",ownerPlayer().getCount(PLAY_NUM)+"");
    }

    public int atk = 1;
    public int hp = 1;

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("疾驰");
        setPlay(new Play(()->{
            addStatus(ownerPlayer().getCount(PLAY_NUM),0);
        }));
    }

}
