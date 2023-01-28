package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.game.PlayerInfo;
import org.example.system.Lists;

import java.util.List;

import static org.example.constant.CounterKey.ALL_COST;


@Getter
@Setter
public class Bahamut extends FollowCard {
    public Integer cost = 9;
    public String name = "巴哈姆特";
    public String job = "中立";
    private List<String> race = Lists.ofStr("龙");
    public String mark = """
        瞬念召唤：回合结束时在卡牌上的总消耗pp>=50，破坏对手牌堆直至5张
        战吼：破坏对手场上全部卡牌
        """;
    public String subMark = "总消耗pp等于{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",ownerPlayer().getCount(ALL_COST)+"");
    }

    public int atk = 13;
    public int hp = 13;

    public Bahamut() {
        setMaxHp(getHp());
        setPlay(new Play(() -> {
                List<AreaCard> area = enemyPlayer().getArea();
                destroy(area);
            }));
        addEffects((new Effect(this,this, EffectTiming.InvocationEnd,
            ()-> ownerPlayer().getCount(ALL_COST) >= 50,
            ()->{
                PlayerInfo oppositePlayer = info.oppositePlayer();
                List<Card> deck = oppositePlayer.getDeck();
                if (deck.size()>5) {
                    deck.removeAll(deck.subList(5,deck.size()));
                }
            })));
    }
}
