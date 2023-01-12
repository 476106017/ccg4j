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
public class PhantomAssassin extends FollowCard {
    private String name = "幻影刺客";
    private Integer cost = 2;
    private int atk = 3;
    private int hp = 1;
    private String job = "dota";
    private List<String> race = Lists.ofStr("天灾军团","英雄");
    private String mark = """
        战吼：获得1张窒息之刃和1张幻影突袭
        攻击时：15%几率造成4.5倍伤害的致命一击
        有35%的几率闪避受到的普攻伤害(包含反击伤害)
        """;
    public String subMark = "";


    public PhantomAssassin() {
        setMaxHp(getHp());
        setPlay(new Play(() -> {
            ownerPlayer().addHand(createCard(StiflingDagger.class));
            ownerPlayer().addHand(createCard(PhantomStrike.class));
        }));

        addEffects(new Effect(this,this,EffectTiming.WhenAttack,obj->{
            if(Math.random()>0.15)return;
            getInfo().msg("恩赐解脱！");
            Damage damage = (Damage) obj;
            int damageInt = damage.getDamage();
            damage.setDamage((int)(damageInt * 4.5));
        }));
        addEffects(new Effect(this,this,EffectTiming.BeforeDamaged,obj->{
            if(Math.random()>0.35)return;

            getInfo().msg("丢失！");
            Damage damage = (Damage) obj;
            damage.setMiss(true);
        }));
    }

    @Getter
    @Setter
    public static class StiflingDagger extends SpellCard {
        public Integer cost = 1;
        public String name = "窒息之刃";
        public String job = "dota";
        private List<String> race = Lists.ofStr("技能");
        public String mark = """
        投出匕首，对目标造成1点伤害和1层【眩晕】
        """;
        public String subMark = "";

        public StiflingDagger() {
            setPlay(new Play(()->enemyPlayer().getAreaFollowsAsGameObj(),true,obj->{
                if(obj instanceof FollowCard followCard){
                    new Damage(getParent(),followCard,1).apply();
                    followCard.addKeyword("眩晕");
                }
            }));
        }
    }
    @Getter
    @Setter
    public static class PhantomStrike extends SpellCard {
        public Integer cost = 1;
        public String name = "幻影突袭";
        public String job = "dota";
        private List<String> race = Lists.ofStr("技能");
        public String mark = """
        立即攻击1个敌方随从，之后重置普攻
        """;
        public String subMark = "";

        public PhantomStrike() {
            setPlay(new Play(()->enemyPlayer().getAreaFollowsAsGameObj(),true,obj->{
                if(obj instanceof FollowCard followCard){
                    FollowCard parent = (FollowCard) getParent();
                    parent.attack(followCard);
                    parent.setTurnAttack(0);
                }
            }));
        }
    }

}