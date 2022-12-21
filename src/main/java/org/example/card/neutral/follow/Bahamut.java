package org.example.card.neutral.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.GameObj;
import org.example.game.PlayerInfo;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Bahamut extends FollowCard {
    public Integer cost = 9;
    public String name = "巴哈姆特";
    public String job = "中立";
    public String race = "龙";
    public String mark = """
        瞬念召唤：回合结束时总消耗pp>=50,破坏对手牌库直至5张
        战吼：破坏对手场上全部卡牌
        """;
    public String subMark = "总消耗pp等于{allCost}";

    public String getSubMark() {
        return subMark.replaceAll("\\{allCost}",
            info.getPlayerInfos()[owner].getCount("allCost")+"");
    }

    public int atk = 50;
    public int hp = 50;
    public int maxHp = 50;

    @Override
    public void fanfare(List<GameObj> targets) {
        super.fanfare(targets);
        List<AreaCard> area = oppositePlayer().getArea();
        info.destroy(area);
        info.msg("随着巴哈姆特的一声怒吼，对面的战场被清理的一干二净");
    }

    public boolean canInvocationEnd() {
        return ownerPlayer().getCount("allCost") >= 50;
    }

    @Override
    public void afterInvocationEnd() {
        PlayerInfo oppositePlayer = info.oppositePlayer();
        List<Card> deck = oppositePlayer.getDeck();
        if (deck.size()>5) {
            deck.removeAll(deck.subList(5,deck.size()));
        }
    }
}
