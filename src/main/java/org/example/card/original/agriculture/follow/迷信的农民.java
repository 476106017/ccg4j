package org.example.card.original.agriculture.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.ccg.sts.spell.Angry;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class 迷信的农民 extends FollowCard {
    private String name = "迷信的农民";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 1;
    private String job = "农业";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：对1名敌方随从造成与【该随从名字长度】等量的伤害，压轴：并获得与【该随从关键词数量】等量攻击力
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->enemyPlayer().getAreaFollowsAsGameObj(),
            true,
            target->{
                info.damageEffect(this,target,target.getName().length());

                if(ownerPlayer().getPpNum()==0){
                    final int size = ((FollowCard) target).getKeywords().size();
                    addStatus(size,0);
                }
            }
        ));
    }

}