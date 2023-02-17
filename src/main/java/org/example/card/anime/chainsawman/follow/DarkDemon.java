package org.example.card.anime.chainsawman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class DarkDemon extends FollowCard {
    private int slot = 7;
    private int apposition = 1;
    private String name = "暗之恶魔";
    private Integer cost = 4;
    private int atk = 4;
    private int hp = 4;
    private String job = "链锯人";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        战吼：除外自己，永久失去己方战场的1格空间。主战者获得唯一效果【我方回合开始时，除外敌方墓地所有牌，并回复等量生命值】
        """;
    private String subMark = "";
    public DarkDemon() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            info.exile(this);
            ownerPlayer().setAreaMax(ownerPlayer().getAreaMax() - 1);

            ownerLeader().addEffect(new Effect(this, ownerLeader(), EffectTiming.BeginTurn,() -> {
                List<Card> graveyard = enemyPlayer().getGraveyardCopy();
                getInfo().exile(graveyard);
                ownerPlayer().heal(graveyard.size());
            }),true);
        }));
    }
}
