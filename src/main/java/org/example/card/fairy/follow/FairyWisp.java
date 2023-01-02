package org.example.card.fairy.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;


@Getter
@Setter
public class FairyWisp extends FollowCard {
    public Integer cost = 0;
    public String name = "妖精萤火";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("妖精");
    public String mark = """
        战吼：如果本回合使用的卡牌数大于2,则本随从消失
        """;
    public String subMark = "本回合使用的卡牌数等于{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",ownerPlayer().getCount(PLAY_NUM)+"");
    }

    public int atk = 1;
    public int hp = 1;

    public FairyWisp() {
        setMaxHp(getHp());
        getPlays().add(new Card.Event.Play(()->{
            if(ownerPlayer().getCount(PLAY_NUM)>=2) this.removeWhenAtArea();
        }));
    }

}
