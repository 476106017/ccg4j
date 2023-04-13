package org.example.constant;

import lombok.Getter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.Damage;

import java.util.List;

@Getter
public enum EffectTiming {
    BeginGame("游戏开始时"),

    BeginTurn("回合开始时"),
    EndTurn("回合结束时"),
    BeginTurnAtHand("在手牌上回合开始时"),
    EndTurnAtHand("在手牌上回合结束时"),
    EnemyBeginTurn("敌方回合开始时"),
    EnemyEndTurn("敌方回合结束时"),
    EnemyBeginTurnAtHand("在手牌上敌方回合开始时"),
    EnemyEndTurnAtHand("在手牌上敌方回合结束时"),


    InvocationBegin("瞬念召唤（回合开始时）"),
    InvocationEnd("瞬念召唤（回合结束时）"),
    WhenPlay("我方出牌时",Card.class),
    WhenEnemyPlay("敌方出牌时",Card.class),
    Boost("增幅",Card.class,true),

    WhenOtherAttack("友方随从攻击时",Damage.class),// TODO

    WhenAttack("攻击时",Damage.class),
    WhenBattle("交战时",Damage.class),
    WhenLeaderSkill("激励时"),
    BeforeDamaged("受伤前",Damage.class),

    // ————————— 以下是可以通过连锁触发的效果 —————————//


    AfterDamaged("受伤时",Damage.class),
    AfterLeaderDamaged("主战者受伤后",Damage.class),
    LeaderHealing("主战者回复时",Damage.class),
    LeaderHealed("主战者回复后",Damage.class),
    WhenAddHand("加入手牌时",List.class),
    WhenAddedToHand("被加入手牌时"),
    WhenDrawn("被抽到时"),
    WhenDraw("我方抽牌时",List.class),
    WhenOverDraw("我方超抽时",Integer.class),
    WhenEnemyDraw("敌方抽牌时",List.class),
    WhenEnemyOverDraw("敌方超抽时",Integer.class),
    WhenAbandoned("被弃牌时"),
    WhenAbandon("弃牌时",List.class),
    WhenSummon("我方召唤时",List.class),
    WhenEnemySummon("敌方召唤时",List.class),
    WhenDestroy("我方破坏时",List.class),
    WhenEnemyDestroy("敌方破坏时",List.class),
    WhenRecalled("被召还时",List.class),
    WhenOthersRecall("其他卡牌召还时",List.class),
    Entering("入场时"),
    WhenAtArea("在场时"),
    WhenNoLongerAtArea("不在场时"),
    Leaving("离场时"),
    WhenBackToHand("返回手牌时"),
    Exile("除外时",Card.class),
    DeathRattle("亡语"),
    WhenCostGraveyard("发动死灵术时",Integer.class),
    WhenCostPartyHot("发动派对狂欢时",Integer.class),
    Transmigration("轮回时"),
    Charge("注能", AreaCard.class,true),
    WhenKill("击杀时",FollowCard.class),

    // 特殊规则事件
    WhenSwapChara("切换时"),

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
