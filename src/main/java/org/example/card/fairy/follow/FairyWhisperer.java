package org.example.card.fairy.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class FairyWhisperer extends FollowCard {
    public Integer cost = 2;
    public String name = "妖之轻语者";
    public String job = "妖精";
    private List<String> race = List.of("人类");
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

    public FairyWhisperer() {
        getPlays().add(new Card.Event.Play(ArrayList::new,0, target->{
            List<Card> fairies = new ArrayList<>();
            for (int i = 0; i < getCost(); i++) {
                Fairy fairy = createCard(Fairy.class);
                fairies.add(fairy);
            }
            ownerPlayer().addHand(fairies);
        }));
    }

}
