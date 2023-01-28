package org.example.card.ccg.warlock.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.game.PlayerInfo;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LastResort extends SpellCard {
    public Integer cost = 6;
    public String name = "终极手段";
    public String job = "术士";
    private List<String> race = Lists.ofStr("邪能");
    public String mark = """
        摧毁我方牌库
        超抽效果改为：回复与疲劳伤害等量的生命值
        抽1张牌
        """;

    public String subMark = "";


    public LastResort() {
        setPlay(new Play(()->{
            PlayerInfo player = ownerPlayer();

            info.exile(player.getDeckCopy());

            ownerLeader().setOverDraw(integer -> {
                for (int i = 0; i < integer; i++) {
                    player.heal(player.countWeary());
                }
            });

            player.draw(1);
        }));
    }

}
