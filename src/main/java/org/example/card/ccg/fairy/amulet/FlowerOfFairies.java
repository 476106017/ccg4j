package org.example.card.ccg.fairy.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class FlowerOfFairies extends AmuletCard {

    public Integer cost = 2;

    public String name = "妖精之花";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("自然");
    public transient int countDown = 2;

    public String mark = """
        战吼：抽1张牌
        亡语/返回手牌时：增加1张妖精萤火到手牌
        """;
    public String subMark = "";

    public FlowerOfFairies() {
        setPlay(new Play(()->{
            ownerPlayer().draw(1);
        }));
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->
            ownerPlayer().addHand(createCard(Derivant.FairyWisp.class))
        )));
        addEffects((new Effect(this,this, EffectTiming.WhenBackToHand, obj->
            ownerPlayer().addHand(createCard(Derivant.FairyWisp.class))
        )));
    }

}
