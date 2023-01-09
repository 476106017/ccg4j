package org.example.constant;

import lombok.Getter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.Damage;

import java.util.List;

@Getter
public enum EffectTiming {
    BeginTurn("回合开始时"),
    EndTurn("回合结束时"),
    EnemyBeginTurn("敌方回合开始时"),
    EnemyEndTurn("敌方回合结束时"),


    InvocationBegin("瞬念召唤（回合开始时）"),
    InvocationEnd("瞬念召唤（回合结束时）"),
    WhenPlay("我方出牌时",Card.class),
    WhenEnemyPlay("敌方出牌时",Card.class),
    Boost("增幅",Card.class,true),

    WhenAttack("攻击时",Damage.class),
    WhenBattle("交战时",Damage.class),
    WhenLeaderSkill("激励时"),
    BeforeDamaged("受伤前",Damage.class),

    // ———————————————————— 以下是可以通过连锁触发的效果 ————————————————————//


    AfterDamaged("受伤时",Damage.class),
    AfterLeaderDamaged("主战者受伤后",Damage.class),
    LeaderHealing("主战者回复时",Damage.class),
    LeaderHealed("主战者回复后",Damage.class),
    WhenDrawn("被抽到时"),
    WhenDraw("我方抽牌时",List.class),
    WhenEnemyDraw("敌方抽牌时",List.class),
    WhenSummon("我方召唤时",List.class),
    WhenEnemySummon("敌方召唤时",List.class),
    Entering("入场时"),
    WhenAtArea("在场时"),
    WhenNoLongerAtArea("不在场时"),
    Leaving("离场时"),
    WhenBackToHand("返回手牌时"),
    Exile("除外时",Card.class),
    DeathRattle("亡语"),
    Transmigration("轮回时"),
    Charge("注能",List.class,true),
    WhenKill("击杀时",FollowCard.class),

    ;

    private String name;
    private Class paramClass = null;
    private boolean secret = false;
    private boolean enableWhenLeaveArea = false; //TODO

    EffectTiming(String name) {
        this.name = name;
    }
    EffectTiming(String name,Class paramClass) {
        this.name = name;
        this.paramClass = paramClass;
    }
    EffectTiming(String name,Class param,boolean secret) {
        this.name = name;
        this.paramClass = param;
        this.secret = secret;
    }
}
