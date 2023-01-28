package org.example.card.ccg.warlock.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class DemonicTorturer extends FollowCard {
    private String name = "恶魔折磨师";
    private Integer cost = 3;
    private int atk = 2;
    private int hp = 4;
    private String job = "术士";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        攻击时：对手受到疲劳伤害
        """;
    private String subMark = "";

    public DemonicTorturer() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            if(ownerPlayer().getDeck().isEmpty()){
                ownerPlayer().addDeck(ownerPlayer().getAbandon().stream().toList());
            }
        }));
    }
}