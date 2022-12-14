package org.example.card.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.Patten;
import org.example.game.GameInfo;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FairyWhisperer extends FollowCard {
    public String name = "妖之轻语者";
    public String job = "";
    public String mark = """
        战吼：获得X张妖精（X是当前分数）
        """;
    public String subMark = "X等于{score}";

    public String getSubMark() {
        return subMark.replaceAll("\\{score}",info.getPlayerInfos()[owner].getScore()+"");
    }

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
