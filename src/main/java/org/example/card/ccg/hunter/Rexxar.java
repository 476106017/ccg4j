package org.example.card.ccg.hunter;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.system.util.Lists;

import java.util.List;
import java.util.function.Consumer;
import org.example.constant.CardRarity;


@Getter
@Setter
public class Rexxar extends Leader {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "雷克萨";
    private String job = "猎人";

    private String skillName = "稳固射击";
    private String skillMark =  """
        对敌方主战者造成2点伤害
        """;
    private int skillCost = 2;

    private boolean needTarget = false;

    private String mark = "炉石传说中的一位英雄";

    private String overDrawMark =  """
        对自己造成疲劳伤害
        """;

    private Consumer<Integer> overDraw = integer -> {
        for (int i = 0; i < integer; i++) {
            ownerPlayer().wearyDamaged();
        }
    };

    @Override
    public void skill(GameObj target) {
        super.skill(target);

        info.damageEffect(this,enemyLeader(),2);
    }

    @Getter
    @Setter
    public static class FoxSpiritWildseed extends AmuletCard {

        private CardRarity rarity = CardRarity.BRONZE;
        public Integer cost = 1;

        public String name = "狐灵之种";
        public String job = "猎人";
        private List<String> race = Lists.ofStr("灵种");

        public String mark = """
        亡语：召唤1个3/1且拥有【突进】的狐灵
        """;
        public String subMark = "";

	    public void init() {
            setCountDown(1);
            addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->
                ownerPlayer().summon(createCard(FoxSpirit.class))
            )));
        }

        @Getter
        @Setter
        public static class FoxSpirit extends FollowCard {

            private CardRarity rarity = CardRarity.BRONZE;
            private String name = "狐灵";
            private Integer cost = 1;
            private int atk = 3;
            private int hp = 1;
            private String job = "猎人";
            private List<String> race = Lists.ofStr("野兽");
            private String mark = "";
            private String subMark = "";

            public void init() {
                setMaxHp(getHp());
                getKeywords().add("突进");
            }
        }
    }
    @Getter
    @Setter
    public static class BearSpiritWildseed extends AmuletCard {

        private CardRarity rarity = CardRarity.BRONZE;
        public Integer cost = 1;

        public String name = "熊灵之种";
        public String job = "猎人";
        private List<String> race = Lists.ofStr("灵种");

        public String mark = """
        亡语：召唤1个2/4且拥有【守护】的熊灵
        """;
        public String subMark = "";

        public void init() {
            setCountDown(2);
            addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->
                ownerPlayer().summon(createCard(BearSpirit.class))
            )));
        }

        @Getter
        @Setter
        public static class BearSpirit extends FollowCard {

            private CardRarity rarity = CardRarity.BRONZE;
            private String name = "熊灵";
            private Integer cost = 2;
            private int atk = 2;
            private int hp = 4;
            private String job = "猎人";
            private List<String> race = Lists.ofStr("野兽");
            private String mark = "";
            private String subMark = "";

            public void init() {
                setMaxHp(getHp());
                getKeywords().add("守护");
            }
        }
    }
    @Getter
    @Setter
    public static class StagSpiritWildseed extends AmuletCard {

        private CardRarity rarity = CardRarity.BRONZE;
        public Integer cost = 1;

        public String name = "鹿灵之种";
        public String job = "猎人";
        private List<String> race = Lists.ofStr("灵种");

        public String mark = """
        亡语：召唤1个4/3的鹿灵，对敌方主战者造成3点伤害
        """;
        public String subMark = "";

        public void init() {
            setCountDown(3);
            addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
                ownerPlayer().summon(createCard(StagSpirit.class));
                info.damageEffect(this,enemyLeader(),3);
            })));
        }

        @Getter
        @Setter
        public static class StagSpirit extends FollowCard {

            private CardRarity rarity = CardRarity.BRONZE;
            private String name = "鹿灵";
            private Integer cost = 3;
            private int atk = 4;
            private int hp = 3;
            private String job = "猎人";
            private List<String> race = Lists.ofStr("野兽");
            private String mark = "";
            private String subMark = "";

            public void init() {
                setMaxHp(getHp());
            }
        }
    }
}
