package org.example.morecard.xie.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Test21 extends FollowCard {
    private String name = "Test21";
    private Integer cost = 4;
    private int atk = 2;
    private int hp = 2;
    private String job = "谢test";
    private List<String> race = Lists.ofStr();
    private String mark = """
        相邻随从获得【守护】、+1/+1
        """;
    private String subMark = "";

    private List<FollowCard> effectedFollow = new ArrayList<>();


    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(this::sh));
    }

    private void sh(){
        List<AreaCard> areaCopy = ownerPlayer().getAreaCopy();
        int i = areaCopy.indexOf(this);
        if(i > 0){
            AreaCard areaCard = areaCopy.get(i - 1);
            if(areaCard instanceof FollowCard followCard && !followCard.hasKeyword("守护")){
                effectedFollow.add(followCard);
                followCard.addKeyword("守护");
                followCard.addStatus(1,1);
            }
        }
        if(i < areaCopy.size()-1){
            AreaCard areaCard = areaCopy.get(i + 1);
            if(areaCard instanceof FollowCard followCard && !followCard.hasKeyword("守护")){
                effectedFollow.add(followCard);
                followCard.addKeyword("守护");
                followCard.addStatus(1,1);
            }
        }
    }
}