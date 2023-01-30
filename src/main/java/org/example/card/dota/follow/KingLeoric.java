package org.example.card.dota.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.*;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class KingLeoric extends FollowCard {
    private String name = "骷髅王";
    private Integer cost = 5;
    private int atk = 2;
    private int hp = 6;
    private String job = "dota";
    private List<String> race = Lists.ofStr("天灾军团","英雄");
    private String mark = """
        战吼：获得1张冥火暴击
        攻击时：15%几率造成3倍伤害的致命一击
        """;
    public String subMark = "";


    public KingLeoric() {
        setMaxHp(getHp());
        getKeywords().add("自愈");
        getKeywords().add("吸血");
        getKeywords().add("复生");
        setPlay(new Play(() -> ownerPlayer().addHand(createCard(StormBolt.class))));

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
    public static class StormBolt extends SpellCard {
        public Integer cost = 2;
        public String name = "冥火暴击";
        public String job = "dota";
        private List<String> race = Lists.ofStr("技能");
        public String mark = """
        从剑尖向目标投掷一团来自地狱的火焰，造成2点伤害和2层【眩晕】
        """;
        public String subMark = "";

        public StormBolt() {
            setPlay(new Play(()->enemyPlayer().getAreaFollowsAsGameObj(),true,obj->{
                if(obj instanceof FollowCard followCard){
                    new Damage(getParent(),followCard,2).apply();
                    followCard.addKeywordN("眩晕", 2);
                }
            }));
        }
    }

}