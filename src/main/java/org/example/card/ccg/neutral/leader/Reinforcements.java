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
 * 援军：2费召唤一个1/1的白板随从
 */
@Getter
@Setter
public class Reinforcements extends Leader {

    private CardRarity rarity = CardRarity.BRONZE;
    private String name = "圣骑士";
    private String job = "中立";

    private String skillName = "援军";
    private String skillMark = """
            召唤一个1/1的白银之手新兵
            """;
    private int skillCost = 2;

    private String mark = "召唤基础随从";

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

        // 召唤一个1/1的白银之手新兵
        FollowCard recruit = new SilverHandRecruit();
        recruit.setOwner(getOwner());
        recruit.setInfo(getInfo());
        ownerPlayer().summon(List.of(recruit));
    }

    /**
     * 白银之手新兵 - 1/1 白板随从
     */
    @Getter
    @Setter
    public static class SilverHandRecruit extends FollowCard {

        private CardRarity rarity = CardRarity.BRONZE;
        private String name = "白银之手新兵";
        private String job = "中立";
        private List<String> race = new ArrayList<>();
        private Integer cost = 1;
        private int atk = 1;
        private int hp = 1;
        // keywords 字段已在 Card 基类中定义，不需要重复声明
        private String mark = "基础的1/1随从";
        private String subMark = "";

        @Override
        public String getSubMark() {
            return subMark;
        }

        @Override
        public void init() {
            setMaxHp(getHp());
        }
    }
}
