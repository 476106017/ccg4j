package org.example.card.original.agriculture.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.ccg.nemesis.spell.CalamitysGenesis;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class 稻草人 extends FollowCard {
    private String name = "稻草人";
    private Integer cost = 1;
    private int atk = 0;
    private int hp = 1;
    private String job = "农业";
    private List<String> race = Lists.ofStr();
    private String mark = """
        交战时：使交战对象-1/-1
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects(new Effect(this,this, EffectTiming.WhenBattle, obj->{
            Damage damage = (Damage) obj;
            FollowCard another = (FollowCard) damage.another(this);
            another.addStatus(-1,-1);
        }));
    }
}