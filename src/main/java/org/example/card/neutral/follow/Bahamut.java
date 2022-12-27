package org.example.card.neutral.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.ALL_COST;

@EqualsAndHashCode(callSuper = true)
@Data
public class Bahamut extends FollowCard {
    public Integer cost = 9;
    public String name = "巴哈姆特";
    public String job = "中立";
    private List<String> race = List.of("龙");
    public String mark = """
        瞬念召唤：回合结束时在卡牌上的总消耗pp>=50,破坏对手牌堆直至5张
        战吼：破坏对手场上全部卡牌
        """;
    public String subMark = "总消耗pp等于{allCost}";

    public String getSubMark() {
        return subMark.replaceAll("\\{allCost}",
            info.getPlayerInfos()[getOwner()].getCount(ALL_COST)+"");
    }

    public int atk = 13;
    public int hp = 13;

    public Bahamut() {
        super();
        getPlays().add(new Card.Event.Play(ArrayList::new,0,
            gameObjs -> {
                List<AreaCard> area = enemyPlayer().getArea();
                info.destroy(area);
                info.msg("随着巴哈姆特的一声怒吼，对面的战场被清理的一干二净");
            }
        ));
        getInvocationEnds().add(new Card.Event.InvocationEnd(
            ()-> ownerPlayer().getCount(ALL_COST) >= 50,
            ()->{
                PlayerInfo oppositePlayer = info.oppositePlayer();
                List<Card> deck = oppositePlayer.getDeck();
                if (deck.size()>5) {
                    deck.removeAll(deck.subList(5,deck.size()));
                }
            }
        ));
    }
}
