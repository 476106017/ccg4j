package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class jzgz extends FollowCard {
    public Integer cost = 4;
    public String name = "举烛观众";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        战吼：使相邻随从获得圣盾
        """;
    public String subMark = "";


    public int atk = 3;
    public int hp = 3;

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("圣盾");
        setPlay(new Play(()->{
            List<AreaCard> areaCopy = ownerPlayer().getAreaCopy();
            int i = areaCopy.indexOf(this);
            if(i > 0){
                AreaCard areaCard = areaCopy.get(i - 1);
                if(areaCard instanceof FollowCard followCard)
                    followCard.addKeyword("圣盾");
            } else if(i < areaCopy.size()-1){
                AreaCard areaCard = areaCopy.get(i + 1);
                if(areaCard instanceof FollowCard followCard)
                    followCard.addKeyword("圣盾");
            }
        }));
    }

}
