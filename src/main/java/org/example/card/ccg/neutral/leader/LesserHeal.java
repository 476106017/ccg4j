package org.example.card.ccg.neutral.leader;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.CardRarity;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 次级治疗术：2费为一个友方角色恢复2点生命值
 */
@Getter
@Setter
public class LesserHeal extends Leader {

    private CardRarity rarity = CardRarity.BRONZE;
    private String name = "牧师";
    private String job = "中立";

    private String skillName = "次级治疗术";
    private String skillMark = """
        为一个友方角色恢复2点生命值
        """;
    private int skillCost = 2;

    private String mark = "基础的治疗技能";

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
        // 可以指定己方随从或己方主战者
        targets.addAll(ownerPlayer().getAreaFollowsAsGameObj());
        targets.add(ownerLeader());
        return targets;
    }

    @Override
    public void skill(GameObj target) {
        super.skill(target);
        
        // 恢复2点生命值
        if (target instanceof FollowCard followCard) {
            followCard.heal(2);
        } else if (target instanceof Leader) {
            ownerPlayer().heal(2);
        }
    }
}
