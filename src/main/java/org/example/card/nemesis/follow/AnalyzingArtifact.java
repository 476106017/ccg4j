package org.example.card.nemesis.follow;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;

import java.util.List;


@Getter
@Setter
public class AnalyzingArtifact  extends FollowCard {
    private String name = "解析的造物";
    private Integer cost = 1;
    private int atk = 2;
    private int hp = 1;
    private String job = "复仇者";
    private List<String> race = List.of("创造物");
    private String mark = """
        亡语：抽1张牌
        """;
    private String subMark = "";


    public AnalyzingArtifact() {
        setMaxHp(getHp());
        getDeathRattles().add(new AreaCard.Event.DeathRattle(()->{
            ownerPlayer().draw(1);
        }));
    }
}
