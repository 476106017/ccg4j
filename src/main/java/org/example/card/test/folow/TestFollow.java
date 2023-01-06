package org.example.card.test.folow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.nemesis.follow.AnalyzingArtifact;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class TestFollow extends FollowCard {
    private String name = "测试随从";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 3;
    private String job = "测试";
    private List<String> race = Lists.ofStr();
    private String mark = """
        触发事件时发出信息
        """;
    private String subMark = "";


    public TestFollow() {
        setMaxHp(getHp());
        getKeywords().add("剧毒");
//        setPlay(new Play(() -> getInfo().msg(getNameWithOwner() + "战吼")));
//        addEffects((new Effect(this,this, EffectTiming.Entering, obj->
//            getInfo().msg(getNameWithOwner() + "入场时"))));
//        addEffects((new Effect(this,this, EffectTiming.Leaving, obj->
//            getInfo().msg(getNameWithOwner() + "离场时"))));
        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->
        {
            getInfo().msg(getNameWithOwner() + "亡语：对对方全部随从造成1点伤害");
            getInfo().damageMulti(this,enemyPlayer().getAreaFollowsAsGameObj(),1);
        })));
//        addEffects((new Effect(this,this, EffectTiming.BeginTurn, obj->
//            getInfo().msg(getNameWithOwner() + "回合开始时"))));
//        addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->
//            getInfo().msg(getNameWithOwner() + "回合结束时"))));
//        addEffects((new Effect(this,this, EffectTiming.WhenAttack,damage->
//            getInfo().msg(getNameWithOwner() + "攻击时"))));
//        addEffects((new Effect(this,this, EffectTiming.WhenBattle,damage->
//            getInfo().msg(getNameWithOwner() + "交战时"))));
        addEffects((new Effect(this,this, EffectTiming.AfterDamaged,damage->
       {
               getInfo().msg(getNameWithOwner() + "受伤时");
               ownerPlayer().summon(createCard(TestFollow.class));
       })));
        addEffects((new Effect(this,this, EffectTiming.WhenKill,damage->
        {
            getInfo().msg(getNameWithOwner() + "击杀时");
            getInfo().damageMulti(this,enemyPlayer().getAreaFollowsAsGameObj(),1);
        })));
//        addEffects((new Effect(this,this, EffectTiming.Exile, obj->
//            getInfo().msg(getNameWithOwner() + "除外时"))));
//        addEffects((new Effect(this,this, EffectTiming.Transmigration, obj->
//            getInfo().msg(getNameWithOwner() + "轮回时"))));
//        addEffects((new Effect(this,this, EffectTiming.WhenLeaderSkill, obj->
//            getInfo().msg(getNameWithOwner() + "激励"))));
//        addEffects((new Effect(this,this, EffectTiming.WhenPlay, obj->
//            getInfo().msg(getNameWithOwner() + "我方出牌时"))));
//        addEffects((new Effect(this,this, EffectTiming.WhenEnemyPlay, obj->
//            getInfo().msg(getNameWithOwner() + "敌方出牌时"))));
//        addEffects((new Effect(this,this, EffectTiming.WhenDraw, obj->
//            getInfo().msg(getNameWithOwner() + "我方抽牌时"))));
//        addEffects((new Effect(this,this, EffectTiming.WhenEnemyDraw, obj->
//            getInfo().msg(getNameWithOwner() + "敌方抽牌时"))));
//        addEffects((new Effect(this,this, EffectTiming.WhenSummon, obj->
//            getInfo().msg(getNameWithOwner() + "我方召唤时"))));
//        addEffects((new Effect(this,this, EffectTiming.WhenEnemySummon, obj->
//            getInfo().msg(getNameWithOwner() + "敌方召唤时"))));
//        addEffects((new Effect(this,this, EffectTiming.WhenDrawn, obj->
//            getInfo().msg(getNameWithOwner() + "被抽到时"))));

    }
}