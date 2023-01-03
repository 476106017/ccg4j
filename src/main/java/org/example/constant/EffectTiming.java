package org.example.constant;

public enum EffectTiming {
    //Area
    BeginTurn,
    EndTurn,
    LeaderBeforeDamaged,
    LeaderAfterDamaged,
    LeaderHealing,
    LeaderHealed,
    Entering,
    Leaving,
    WhenBackToHand,
    WhenAtArea,
    WhenNoLongerAtArea,
    DeathRattle,
    WhenSummon,
    WhenEnemySummon,
    WhenDraw,

    // Card
    Play,
    InvocationBegin,
    InvocationEnd,
    Transmigration,
    Exile,
    Boost,
    Charge,
    WhenKill,
    WhenDrawn,


    // Follow
    WhenAttack,
    WhenBattle,
    AfterDamaged,
    WhenLeaderSkill,
}
