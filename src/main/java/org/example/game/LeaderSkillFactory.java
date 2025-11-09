package org.example.game;

import org.example.card.ccg.neutral.leader.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 英雄技能工厂 - 随机分配英雄技能
 */
public class LeaderSkillFactory {
    
    private static final Random random = new Random();
    
    /**
     * 所有可用的英雄技能类
     */
    private static final List<Class<? extends Leader>> AVAILABLE_SKILLS = List.of(
        Evolution.class,        // 进化：0费，使一个随从+2/+2突进（有次数限制）
        FlameShock.class,       // 火焰冲击：2费，造成1点伤害
        LifeTap.class,          // 生命分流：2费，抽1张牌并受3点伤害
        LesserHeal.class,       // 次级治疗术：2费，恢复2点生命值
        Reinforcements.class    // 援军：2费，召唤1/1随从
    );
    
    /**
     * 随机获取一个英雄技能类
     */
    public static Class<? extends Leader> getRandomSkill() {
        int index = random.nextInt(AVAILABLE_SKILLS.size());
        return AVAILABLE_SKILLS.get(index);
    }
    
    /**
     * 获取所有可用的技能列表（用于展示）
     */
    public static List<Class<? extends Leader>> getAllSkills() {
        return new ArrayList<>(AVAILABLE_SKILLS);
    }
}
