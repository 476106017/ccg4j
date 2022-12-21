package org.example.card.nemesis.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.FollowCard;

@EqualsAndHashCode(callSuper = true)
@Data
public class AnalyzingArtifact  extends FollowCard {
    private Integer cost = 1;
    private String name = "解析的造物";
    private String job = "复仇者";
    private String race = "创造物";
    private String mark = """
        亡语：抽一张牌
        """;
    private String subMark = "";

    private int atk = 2;
    private int hp = 1;
    private int maxHp = 1;

    @Override
    public void deathrattle() {
        ownerPlayer().draw(1);
    }
}
