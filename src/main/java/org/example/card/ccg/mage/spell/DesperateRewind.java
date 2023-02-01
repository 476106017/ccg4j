package org.example.card.ccg.mage.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class DesperateRewind extends SpellCard {
    public Integer cost = 7;
    public String name = "绝望回溯";
    public String job = "法师";
    private List<String> race = Lists.ofStr();
    public String mark = """
        主战者回复40点生命
        使超抽效果变成：输掉游戏
        """;

    public String subMark = "";


    public DesperateRewind() {
        setPlay(new Play(()->{
            ownerPlayer().heal(40);
            ownerLeader().setOverDraw(integer -> getInfo().gameset(enemyPlayer()));
            ownerLeader().setOverDrawMark("输掉游戏");
        }));
    }

}
