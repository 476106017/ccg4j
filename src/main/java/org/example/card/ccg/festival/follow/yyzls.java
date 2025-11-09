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
public class yyzls extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 2;
    public String name = "音乐治疗师";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        战吼：压轴：获得【吸血】
        """;
    public String subMark = "";

    public int atk = 2;
    public int hp = 3;


    public void init() {
        setMaxHp(getHp());
        getKeywords().add("突进");

        setPlay(new Play(()->{
            if(ownerPlayer().getPpNum()==0)
                addKeyword("吸血");
        }));
    }

}
