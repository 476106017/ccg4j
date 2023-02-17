package org.example.card.ccg.warlock.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class MistressOfDeath extends FollowCard {
    private String name = "死亡侍女";
    private Integer cost = 3;
    private int atk = 5;
    private int hp = 3;
    private String job = "术士";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        战吼、亡语：我方主战者受到疲劳伤害
        """;
    private String subMark = "";

    public MistressOfDeath() {
        setMaxHp(getHp());
        setPlay(new Play(()-> ownerPlayer().wearyDamaged()));
        addEffects((new Effect(this,this, EffectTiming.DeathRattle,
            ()-> ownerPlayer().wearyDamaged())));

    }
}