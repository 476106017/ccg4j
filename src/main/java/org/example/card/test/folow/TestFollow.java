package org.example.card.test.folow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class TestFollow extends FollowCard {
    private String name = "测试随从";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 2;
    private String job = "测试";
    private List<String> race = Lists.ofStr();
    private String mark = """
        触发事件时发出信息
        """;
    private String subMark = "";


    public TestFollow() {
        setMaxHp(getHp());
        getKeywords().add("剧毒");
        setPlay(new Play(() -> getInfo().msg(getNameWithOwner() + "战吼")));
        getEffects().add(new Effect(this,this, EffectTiming.Entering,)->
            getInfo().msg(getNameWithOwner() + "入场时")));
        getEffects().add(new Effect(this,this, EffectTiming.Leaving,)->
            getInfo().msg(getNameWithOwner() + "离场时")));
        getEffects().add(new Effect(this,this, EffectTiming.DeathRattle,)->
            getInfo().msg(getNameWithOwner() + "亡语")));
        getEffects().add(new Effect(this,this, EffectTiming.EffectBegin,)->
            getInfo().msg(getNameWithOwner() + "回合开始时")));
        getEffects().add(new Effect(this,this, EffectTiming.EffectEnd,)->
            getInfo().msg(getNameWithOwner() + "回合结束时")));
        getEffects().add(new Effect(this,this, EffectTiming.WhenAttack,damage->
            getInfo().msg(getNameWithOwner() + "攻击时")));
        getEffects().add(new Effect(this,this, EffectTiming.WhenBattle,damage->
            getInfo().msg(getNameWithOwner() + "交战时")));
        getEffects().add(new Effect(this,this, EffectTiming.AfterDamaged,damage->
            getInfo().msg(getNameWithOwner() + "受伤时")));
        getEffects().add(new Effect(this,this, EffectTiming.WhenKill,damage->
            getInfo().msg(getNameWithOwner() + "击杀时")));
        getEffects().add(new Effect(this,this, EffectTiming.Exile,)->
            getInfo().msg(getNameWithOwner() + "除外时")));
        getEffects().add(new Effect(this,this, EffectTiming.Transmigration,)->
            getInfo().msg(getNameWithOwner() + "轮回时")));
        getEffects().add(new Effect(this,this, EffectTiming.WhenLeaderSkill,)->
            getInfo().msg(getNameWithOwner() + "激励")));

    }
}