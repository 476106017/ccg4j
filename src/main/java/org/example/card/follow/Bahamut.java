package org.example.card.follow;

import org.example.card.FollowCard;
import org.example.constant.Patten;
import org.example.game.GameInfo;

public class Bahamut extends FollowCard {
    public final String NAME = "巴哈姆特";
    public String MARK = """
        瞬念召唤：YAHTZEE>0
        入场时：破坏对手场上所有卡牌
        """;

    public int atk = 50;
    public int hp = 50;
    public int maxHp = 50;
    public String job = "龙";
    {pattens.add(Patten.Yatzy);}

    public void entering() {
        GameInfo.PlayerInfo oppositePlayer = info.oppositePlayer();
        oppositePlayer.addGraveyard(oppositePlayer.getArea());
        oppositePlayer.getArea().clear();
    }

    public boolean canSuperCantrip() {
        return Patten.Yatzy.getScore(info) >0;
    }
}
