package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class AzsharanSaber extends FollowCard {
    private String name = "艾萨拉的刃豹";
    private Integer cost = 4;
    private int atk = 4;
    private int hp = 3;
    private String job = "猎人";
    private List<String> race = Lists.ofStr("野兽");
    private String mark = """
        亡语：将1张沉没的刃豹放到牌堆底部
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("突进");
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
            ownerPlayer().addDeckBottom(createCard(SunkenSaber.class));
        })));
    }

    @Getter
    @Setter
    public static class SunkenSaber extends FollowCard {
        private String name = "沉没的刃豹";
        private Integer cost = 4;
        private int atk = 4;
        private int hp = 3;
        private String job = "猎人";
        private List<String> race = Lists.ofStr("野兽");
        private String mark = """
            亡语：招募1只野兽
            """;
        private String subMark = "";

        public SunkenSaber(){
            setMaxHp(getHp());
            getKeywords().add("突进");
            addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
                ownerPlayer().hire(card -> card instanceof FollowCard && card.hasRace("野兽"));
            })));
        }
    }

    }