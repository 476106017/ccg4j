package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class gjdyz extends FollowCard {
    public Integer cost = 5;
    public String name = "歌剧独演者";
    public String job = "术士";
    private List<String> race = Lists.ofStr("恶魔");
    public String mark = """
        战吼：如果你没有控制其他随从，对所有敌方随从造成3点伤害
        """;
    public String subMark = "";

    public int atk = 4;
    public int hp = 6;

    public gjdyz() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            if(ownerPlayer().getAreaFollowsBy(p->p!=this).isEmpty()){
                info.damageMulti(this,enemyPlayer().getAreaFollowsAsGameObj(),3);
            }
        }));
    }

}
