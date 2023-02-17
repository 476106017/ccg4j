package org.example.card.paripi.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.paripi.Kongming;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class EarthMagicians extends FollowCard {
    private String name = "疯狂的土系魔法师";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 2;
    private String job = "派对咖";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：对所有随从造成1点伤害。派对热度 3：重复1次。
        """;
    private String subMark = "";

    public EarthMagicians() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            info.damageMulti(this,info.getAreaFollowsAsGameObj(),1);
            if(ownerLeader() instanceof Kongming kongming){
                kongming.costPartyHotTo(3,()->
                    info.damageMulti(this,info.getAreaFollowsAsGameObj(),1));
            }
        }));

    }
}