package org.example.card.dota.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class FacelessVoid extends FollowCard {
    private String name = "虚空假面";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 2;
    private String job = "dota";
    private List<String> race = Lists.ofStr("天灾军团");
    private String mark = """
        战吼：获得1张时间结界
        交战时：有25%的几率给予目标1层【眩晕】且重置攻击次数
        """;
    private String subMark = "";


    public FacelessVoid() {
        setMaxHp(getHp());
        getKeywords().add("突进");
        setPlay(new Play(() -> {
            ownerPlayer().addHand(createCard(Chronosphere.class));
        }));

        addEffects(new Effect(this,this,EffectTiming.WhenBattle,obj->{
            Damage damage = (Damage) obj;
            if(damage.another(this) instanceof FollowCard toFollow){
                toFollow.addKeyword("眩晕");
                setTurnAttack(0);
            }
        }));
    }

    @Getter
    @Setter
    public static class Chronosphere extends SpellCard {
        public Integer cost = 4;
        public String name = "时间结界";
        public String job = "dota";
        private List<String> race = Lists.ofStr();
        public String mark = """
        虚空假面获得【疾驰】并且每回合可以攻击2次
        """;
        public String subMark = "";

        public Chronosphere() {
            setPlay(new Play(()->{
                FollowCard faceless = (FollowCard) getParent();
                faceless.addKeyword("疾驰");
                faceless.setTurnAttackMax(2);
            }));
        }
    }
}