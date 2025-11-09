package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class zyfn extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 4;
    public String name = "自由飞鸟";
    public String job = "中立";
    private List<String> race = Lists.ofStr("野兽");
    public String mark = """
        战吼：获得+X/+X（X是本局对战中使用自由飞鸟的数量）
        """;
    public String subMark = "X等于{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",ownerPlayer().getCount(getName())+"");
    }

    public int atk = 2;
    public int hp = 2;

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("疾驰");
        setPlay(new Play(()->{
            Integer x = ownerPlayer().getCount(getName());
            if(x>0) addStatus(x,x);

            ownerPlayer().count(getName());
        }));
    }

}
