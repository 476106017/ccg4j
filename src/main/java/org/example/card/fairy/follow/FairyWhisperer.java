package org.example.card.fairy.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.fairy.follow.Fairy;
import org.example.game.GameObj;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FairyWhisperer extends FollowCard {
    public Integer cost = 2;
    public String name = "妖之轻语者";
    public String job = "妖精";
    public String race = "人类";
    public String mark = """
        战吼：获得X张妖精（X是当前费用）
        """;
    public String subMark = "X等于{score}";

    public String getSubMark() {
        return subMark.replaceAll("\\{score}",getCost()+"");
    }

    public int atk = 1;
    public int hp = 1;
    public int maxHp = 1;

    @Override
    public void fanfare(List<GameObj> targets) {
        info.msg(getName() + "发动战吼！");
        List<Card> fairies = new ArrayList<>();
        for (int i = 0; i < getCost(); i++) {
            Fairy fairy = createCard(Fairy.class);
            fairies.add(fairy);
        }
        ownerPlayer().addHand(fairies);
    }
}
