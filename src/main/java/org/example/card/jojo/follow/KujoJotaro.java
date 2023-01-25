package org.example.card.jojo.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class KujoJotaro extends FollowCard {
    private String name = "空条承太郎";
    private Integer cost = 7;
    private int atk = 5;
    private int hp = 17;
    private String job = "jojo";
    private List<String> race = Lists.ofStr("替身使者","乔斯达家族");
    private String mark = """
        战吼：攻击敌方最前排随从（若敌方无随从则攻击主战者），重复3次
        """;
    private String subMark = "";

    public KujoJotaro() {
        setMaxHp(getHp());
        setPlay(new Play(() ->{
            info.msg("欧拉欧拉欧拉！");
            for (int i = 0; i < 3; i++) {
                if(!atArea())return;

                List<AreaCard> areaFollows = enemyPlayer().getAreaFollows();
                if(areaFollows.isEmpty())
                    attack(enemyPlayer().getLeader());
                else
                    attack(areaFollows.get(0));
            }
        }));
    }

}