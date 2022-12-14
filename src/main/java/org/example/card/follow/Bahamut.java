package org.example.card.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.FollowCard;
import org.example.constant.Patten;
import org.example.game.GameInfo;

@EqualsAndHashCode(callSuper = true)
@Data
public class Bahamut extends FollowCard {
    public String name = "巴哈姆特";
    public String job = "龙";
    public String mark = """
        瞬念召唤：YAHTZEE>0
        入场时：破坏对手场上所有卡牌
        """;
    public String subMark = "";

    public int atk = 50;
    public int hp = 50;
    public int maxHp = 50;
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
