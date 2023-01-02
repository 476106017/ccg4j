package org.example.card.fairy.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
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
        getPlays().add(new Card.Event.Play(
            ()->enemyPlayer().getAreaFollowsAsGameObj(), 1,
            targets->{
                FollowCard followCard = (FollowCard) targets.get(0);
                followCard.damaged(this,ownerPlayer().getCount(PLAY_NUM));
            }));
    }
}
