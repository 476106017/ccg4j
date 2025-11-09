package org.example.card.ccg.neutral.leader;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.CardRarity;
import org.example.game.Damage;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 生命分流：2费抽1张牌并受到3点伤害
 */
@Getter
@Setter
public class LifeTap extends Leader {

    private CardRarity rarity = CardRarity.BRONZE;
    private String name = "术士";
    private String job = "中立";

    private String skillName = "生命分流";
    private String skillMark = """
        抽1张牌，并受到3点伤害
        """;
    private int skillCost = 2;

    private String mark = "用生命换取牌";

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
        setNeedTarget(false);
    }

    @Override
    public List<GameObj> targetable() {
        return new ArrayList<>();
    }

    @Override
    public void skill(GameObj target) {
        super.skill(target);
        
        // 抽1张牌
        ownerPlayer().draw(1);
        
        // 受到3点伤害
        new Damage(this, this, 3).apply();
    }
}
