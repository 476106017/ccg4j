package org.example.card.anime.jojo.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class JolyneCujoh extends FollowCard {
    private String name = "空条徐伦";
    private Integer cost = 4;
    private int atk = 3;
    private int hp = 3;
    private String job = "jojo";
    private List<String> race = Lists.ofStr("替身使者","乔斯达家族");
    private String mark = """
        1回合可以攻击2次
        """;
    private String subMark = "";

    public JolyneCujoh() {
        setMaxHp(getHp());
        getKeywords().add("突进");
        getKeywords().add("远程");
        setTurnAttackMax(2);
        setPlay(new Play(() ->
            info.msg("空条徐伦：我必会在这片「石之海」当中重获自由！")
        ));
    }

}