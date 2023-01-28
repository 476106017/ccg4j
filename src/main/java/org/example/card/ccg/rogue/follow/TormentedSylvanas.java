package org.example.card.ccg.rogue.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class TormentedSylvanas extends FollowCard {
    private String name = "饱受折磨的希尔瓦娜斯";
    private Integer cost = 8;
    private int atk = 3;
    private int hp = 6;
    private String job = "潜行者";
    private List<String> race = Lists.ofStr();
    private String mark = """
        回合开始时：从对手牌堆中偷取3张牌，如果可偷取的牌不足3张，则输掉游戏
        """;
    private String subMark = "";

    public TormentedSylvanas() {
        setMaxHp(getHp());

        addEffects(new Effect(this,this, EffectTiming.BeginTurn,()->{
            if(enemyPlayer().getDeck().size()<3)
                info.gameset(enemyPlayer());

            ownerPlayer().steal(3);
        }));
    }
}
