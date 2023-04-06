package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.ALL_COST;
import static org.example.constant.CounterKey.PLAY_NUM_ALL;


@Getter
@Setter
public class TheGalaxyGuide extends FollowCard {
    private String name = "银河系向导";
    private Integer cost = 3;
    private int atk = 4;
    private int hp = 2;
    private String job = "中立";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：如果在卡牌上的总消耗pp恰好是42，使对手pp最大值归零；
        如果打出卡牌数恰好是42，除外对方所有的牌
        """;
    public String subMark = "总消耗pp等于{P}，打出卡牌数等于{C}";

    public String getSubMark() {
        return subMark.replaceAll("\\{P}",ownerPlayer().getCount(ALL_COST)+"")
            .replaceAll("\\{C}",ownerPlayer().getCount(PLAY_NUM_ALL)+"");
    }

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->
        {
            if (ownerPlayer().getCount(ALL_COST) == 42){
                info.msg("在卡牌上的总消耗pp恰好是42，使对手pp最大值归零！");
                enemyPlayer().setPpMax(0);
            }
            if (ownerPlayer().getCount(PLAY_NUM_ALL) == 42){
                info.msg("打出卡牌数恰好是42，除外对方所有的牌！");
                info.exile(enemyPlayer().getDeckCopy());
                info.exile(enemyPlayer().getHandCopy());
                info.exile(enemyPlayer().getAreaAsCard());
                info.exile(enemyPlayer().getGraveyardCopy());
            }
            info.msg("什么也没有发生！");
        }));
    }
}