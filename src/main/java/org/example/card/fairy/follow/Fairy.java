package org.example.card.fairy.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Fairy extends FollowCard {
    public Integer cost = 1;

    public String name = "妖精";
    public String job = "妖精";
    private List<String> race = List.of("妖精");
    public String mark = """
        瞬念召唤：回合结束时剩余1pp（不多不少）
        """;
    public String subMark = "";

    public int atk = 1;
    public int hp = 1;
    public int maxHp = 1;

    public Fairy() {
        getInvocationEnds().add(new Card.Event.InvocationEnd(
            ()->ownerPlayer().getPpNum() == 1,
            ()->{}
        ));
    }
}
