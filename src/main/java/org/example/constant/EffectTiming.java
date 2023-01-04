package org.example.constant;

import lombok.Getter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.Damage;

import java.util.List;

@Getter
public enum EffectTiming {
    //Area
    BeginTurn("回合开始时"),
    EndTurn("回合结束时"),
    BeforeLeaderDamaged("主战者受伤时",Damage.class),
    AfterLeaderDamaged("主战者受伤后",Damage.class),
    LeaderHealing("主战者回复时",Damage.class),
    LeaderHealed("主战者回复后",Damage.class),
    Entering("入场时"),
    Leaving("离场时"),
    WhenBackToHand("返回手牌时"),
    WhenAtArea("在场时"),
    WhenNoLongerAtArea("不在场时"),
    DeathRattle("亡语"),
    /**
     * 我方召唤（召唤的卡牌）
     */
    WhenSummon("我方召唤时",AreaCard.class),
    /**
     * 敌方召唤（召唤的卡牌）
     */
    WhenEnemySummon("敌方召唤时",AreaCard.class),
    /**
     * 我方抽牌（抽到的卡牌）
     */
    WhenDraw("我方抽牌时",List.class),
    /**
     * 敌方抽牌（抽到的卡牌）
     */
    WhenEnemyDraw("敌方抽牌时",List.class),

    // Card
    InvocationBegin("瞬念召唤（回合开始时）"),
    InvocationEnd("瞬念召唤（回合结束时）"),
    Transmigration("轮回时"),
    /**
     * 除外（效果发起方）
     */
    Exile("除外时",Card.class),
    /**
     * 增幅（使用的卡牌）
     */
    Boost("增幅",Card.class,true),
    /**
     * 注能（破坏的卡牌）
     */
    Charge("注能",List.class,true),
    /**
     * 击杀时（破坏的随从）
     */
    WhenKill("击杀时",FollowCard.class),
    /**
     * 抽到时
     */
    WhenDrawn("被抽到时"),


    // Follow
    /**
     * 攻击时（攻击伤害）
     */
    WhenAttack("攻击时",Damage.class),
    /**
     * 交战时（攻击/反击伤害）
     */
    WhenBattle("交战时",Damage.class),
    /**
     * 受伤时（攻击伤害）
     */
    AfterDamaged("受伤时",Damage.class),
    /**
     * 激励时
     */
    WhenLeaderSkill("激励时"),
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
