package org.example.card.ccg.fairy.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;


@Getter
@Setter
public class InsectLord extends FollowCard {
    public Integer cost = 2;
    public int atk = 1;
    public int hp = 1;

    public String name = "昆虫王";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("昆虫");
    public String mark = """
        战吼：对敌方场上一名随从造成X点伤害(X是本回合使用的卡牌数)
        """;
    public String subMark = "X等于{}";
    public String getSubMark() {
        return subMark.replaceAll("\\{}",ownerPlayer().getCount(PLAY_NUM)+"");
    }

    public InsectLord() {
        setMaxHp(getHp());
        setPlay(new Play(
            ()->enemyPlayer().getAreaFollowsAsGameObj(), false,
            targets->{
                FollowCard followCard = (FollowCard) targets;
                info.damageEffect(this,followCard,ownerPlayer().getCount(PLAY_NUM));
            }));
    }
}
