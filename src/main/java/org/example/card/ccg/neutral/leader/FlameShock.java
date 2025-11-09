package org.example.card.ccg.neutral.leader;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.CardRarity;
import org.example.game.Damage;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 火焰冲击：2费对敌方目标造成1点伤害
 */
@Getter
@Setter
public class FlameShock extends Leader {

    private CardRarity rarity = CardRarity.BRONZE;
    private String name = "萨满";
    private String job = "中立";

    private String skillName = "火焰冲击";
    private String skillMark = """
        对一个敌方角色造成1点伤害
        """;
    private int skillCost = 2;

    private String mark = "基础的伤害技能";

    private String overDrawMark = """
        对自己造成疲劳伤害
        """;

    private Consumer<Integer> overDraw = integer -> {
        for (int i = 0; i < integer; i++) {
            ownerPlayer().wearyDamaged();
        }
    };

    @Override
    public void init() {
        // 每回合可用一次
    }

    @Override
    public List<GameObj> targetable() {
        List<GameObj> targets = new ArrayList<>();
        // 可以指定敌方随从或敌方主战者
        targets.addAll(enemyPlayer().getAreaFollowsAsGameObj());
        targets.add(enemyLeader());
        return targets;
    }

    @Override
    public void skill(GameObj target) {
        super.skill(target);
        
        // 造成1点伤害
        new Damage(this, target, 1).apply();
    }
}
