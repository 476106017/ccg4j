package org.example.card.nemesis.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class WorldEliminator extends FollowCard {
    private Integer cost = 5;
    private String name = "世界驱除者";
    private String job = "复仇者";

    private List<String> race = new ArrayList<>();
    private String mark = """
        突进
        亡语：使自己主战者hp最大值+2，回复2hp
        """;
    private String subMark = "";

    private int atk = 3;
    private int hp = 3;

    public WorldEliminator() {
        setMaxHp(getHp());
        getKeywords().add("突进");
        getDeathRattles().add(new AreaCard.Event.DeathRattle(()->{
            ownerPlayer().addHpMax(2);
            ownerPlayer().heal(2);
        }));
    }

}
