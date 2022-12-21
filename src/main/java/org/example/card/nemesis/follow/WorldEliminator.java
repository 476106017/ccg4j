package org.example.card.nemesis.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.FollowCard;

@EqualsAndHashCode(callSuper = true)
@Data
public class WorldEliminator extends FollowCard {
    public Integer cost = 5;
    public String name = "世界驱除者";
    public String job = "复仇者";
    public String race = "";
    public boolean isDash = true;
    public String mark = """
        突进
        亡语：使自己主战者hp最大值+2，回复2hp
        """;
    public String subMark = "";

    public int atk = 3;
    public int hp = 3;
    public int maxHp = 3;

    @Override
    public void deathrattle() {
        ownerPlayer().addHpMax(2);
        ownerPlayer().heal(2);
    }
}
