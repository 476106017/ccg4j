package org.example.card.ccg.fairy.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card._derivant.Derivant;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class FairyWhisperer extends FollowCard {
    public Integer cost = 2;
    public String name = "妖之轻语者";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("人类");
    public String mark = """
        战吼：获得X张妖精（X是当前费用）
        """;
    public String subMark = "X等于{score}";

    public String getSubMark() {
        return subMark.replaceAll("\\{score}",getCost()+"");
    }

    public int atk = 1;
    public int hp = 1;

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            List<Card> fairies = new ArrayList<>();
            for (int i = 0; i < getCost(); i++) {
                Derivant.Fairy fairy = createCard(Derivant.Fairy.class);
                fairies.add(fairy);
            }
            ownerPlayer().addHand(fairies);
        }));
    }

}
