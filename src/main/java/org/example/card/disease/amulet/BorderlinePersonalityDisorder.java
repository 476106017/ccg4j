package org.example.card.disease.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class BorderlinePersonalityDisorder extends AmuletCard {

    public Integer cost = 6;

    public String name = "边缘型人格障碍";
    public String job = "疾病";
    private List<String> race = Lists.ofStr();

    public String mark = """
        战吼：手牌和牌堆的所有牌获得【速攻】
        亡语：永远跳过自己的回合
        """;
    public String subMark = "";

    public void init() {
        setCountDown(3);
        setPlay(new Play(()-> {
            ownerPlayer().getHand().forEach(card -> card.addKeyword("速攻"));
            ownerPlayer().getDeck().forEach(card -> card.addKeyword("速攻"));
        }));
        addEffects(new Effect(this,this, EffectTiming.DeathRattle, obj->{

            // 创建主战者回合开始效果
            ownerLeader().addEffect(new Effect(
                this,ownerLeader(),EffectTiming.BeginTurn,
                () -> info.endTurnOfCommand()));
        }));
    }

}
