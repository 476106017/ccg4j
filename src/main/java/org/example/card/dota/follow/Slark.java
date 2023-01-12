package org.example.card.dota.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.*;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class Slark extends FollowCard {
    private String name = "鱼人夜行者";
    private Integer cost = 4;
    private int atk = 1;
    private int hp = 1;
    private String job = "dota";
    private List<String> race = Lists.ofStr("天灾军团","英雄");
    private String mark = """
        战吼：获得1张黑暗契约
        攻击时：如果攻击随从，则使其-1/-1并使自己+1/+1
        回合结束时：如果对方场上没有随从，则回满hp
        """;
    public String subMark = "";


    public Slark() {
        setMaxHp(getHp());
        getKeywords().add("突袭");
        setPlay(new Play(() -> ownerPlayer().addHand(createCard(DarkPact.class))));

        addEffects(new Effect(this,this,EffectTiming.WhenAttack,obj->{
            if(Math.random()>0.15)return;
            getInfo().msg("致命一击！");

            Damage damage = (Damage) obj;
            int damageInt = damage.getDamage();
            damage.setDamage(damageInt * 3);
        }));
    }

    @Getter
    @Setter
    public static class DarkPact extends SpellCard {
        public Integer cost = 0;
        public String name = "黑暗契约";
        public String job = "dota";
        private List<String> race = Lists.ofStr("技能");
        public String mark = """
        支付1hp，在下个回合开始时移除自身的负面关键字，然后再获得1张黑暗契约
        """;
        public String subMark = "";

        public DarkPact() {
            setPlay(new Play(()->{
                Slark slark = (Slark)getParent();
                new Damage(this, slark, slark.getHp()>1?1:0).apply();

                Leader leader = ownerPlayer().getLeader();
                leader.addEffect(new Effect(this,leader,EffectTiming.BeginTurn,3,()->{
                    slark.removeKeywordAll("眩晕");
                    slark.removeKeywordAll("缴械");
                    slark.removeKeywordAll("无法回复");
                    slark.removeKeywordAll("游魂");
                    ownerPlayer().addHand(createCard(DarkPact.class));
                }),true);
            }));
        }
    }

}