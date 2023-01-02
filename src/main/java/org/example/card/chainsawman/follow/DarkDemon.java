package org.example.card.chainsawman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.system.Lists;

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
        亡语：主战者获得唯一效果【己方回合开始时，如果本卡在己方墓地，则除外敌方墓地所有牌】
        """;
    private String subMark = "";
    public DarkDemon() {
        setMaxHp(getHp());
        getKeywords().add("恶魔转生");
        getDeathRattles().add(new AreaCard.Event.DeathRattle(()->{
            // 如果没有获得过该效果
            ownerPlayer().getLeader().addEffect(this, EffectTiming.BeginTurn,() -> {
                if(this.atGraveyard()){
                    List<Card> graveyard = enemyPlayer().getGraveyardCopy();
                    enemyPlayer().countToGraveyard(-graveyard.size());
                    getInfo().exile(graveyard);
                }
            });
        }));
    }
}
