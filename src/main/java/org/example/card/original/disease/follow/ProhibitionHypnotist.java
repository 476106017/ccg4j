package org.example.card.original.disease.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class ProhibitionHypnotist extends FollowCard {
    private String name = "禁术催眠师";
    private Integer cost = 3;
    private int atk = 1;
    private int hp = 1;
    private String job = "疾病";
    private List<String> race = Lists.ofStr();
    private String mark = """
        对手使用卡牌后，结束回合
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects(new Effect(this,this, EffectTiming.AfterEnemyPlay, obj->{
            getInfo().endTurnOfCommand();
        }));
    }
}