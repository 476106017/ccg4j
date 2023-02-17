package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card._derivant.Derivant;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class CorpseBride extends FollowCard {
    private String name = "僵尸新娘";
    private Integer cost = 5;
    private int atk = 4;
    private int hp = 4;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：召唤1个怨灵，死灵术 X：这个怨灵获得+X/+X
        （X不大于8）
        """;
    private String subMark = "";

    public CorpseBride() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            Derivant.Ghost ghost = createCard(Derivant.Ghost.class);
            ownerPlayer().costMoreGraveyardCountTo(8,x -> ghost.addStatus(x,x));
        }));
    }
}