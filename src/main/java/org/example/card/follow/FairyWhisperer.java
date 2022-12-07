package org.example.card.follow;

import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.Patten;
import org.example.game.GameInfo;

import java.util.ArrayList;
import java.util.List;

public class FairyWhisperer extends FollowCard {
    public final String NAME = "妖之轻语者";
    public final String MARK = """
        战吼：获得X张妖精（X是当前分数）
        """;
    public String subMark = "X等于{score}";

    public int atk = 1;
    public int hp = 1;
    public int maxHp = 1;
    {pattens.add(Patten.Twos);}

    public void fanfare() {
        List<Card> fairies = new ArrayList<>();
        for (int i = 0; i < score(); i++) {
            fairies.add(new Fairy());
        }
        info.thisPlayer().addHand(fairies);
    }
}
