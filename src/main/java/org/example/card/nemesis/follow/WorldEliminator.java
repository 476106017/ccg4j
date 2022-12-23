package org.example.card.nemesis.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.FollowCard;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class WorldEliminator extends FollowCard {
    private Integer cost = 5;
    private String name = "世界驱除者";
    private String job = "复仇者";

    private List<String> race = new ArrayList<>();
    private boolean isDash = true;
    private String mark = """
        突进
        亡语：使自己主战者hp最大值+2，回复2hp
        """;
    private String subMark = "";

    private int atk = 3;
    private int hp = 3;
    private int maxHp = 3;

    public WorldEliminator() {
        getDeathRattles().add(new Event.DeathRattle(()->{
            ownerPlayer().addHpMax(2);
            ownerPlayer().heal(2);
        }));
    }

}
