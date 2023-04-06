package org.example.card.ccg.nemesis.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;

@Getter
@Setter
public class TimeEater extends AmuletCard {
    public Integer cost = 0;
    public String name = "时间吞噬者";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr();
    public String mark = """
        当打出第12张牌以上时，回复PP到最大值并受到疲劳伤害
        """;

    public String subMark = "";

    public void init() {
        addEffects((new Effect(this,this, EffectTiming.WhenPlay,
            ()->ownerPlayer().getCount(PLAY_NUM)>=11,
            ()->{
                ownerPlayer().setPpNum(ownerPlayer().getPpMax());
                ownerPlayer().wearyDamaged();
        })));
    }
}
