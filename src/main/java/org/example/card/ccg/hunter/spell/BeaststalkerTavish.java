package org.example.card.ccg.hunter.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.SpellCard;
import org.example.card.ccg.hunter.BeaststalkerTavishLeader;
import org.example.card.ccg.hunter.Rexxar;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class BeaststalkerTavish extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 6;
    public String name = "野兽追猎者塔维什";
    public String job = "猎人";
    private List<String> race = Lists.ofStr("英雄");
    public String mark = """
        获得5层【格挡】，随机召唤2只强化的灵种
        主战者变为【野兽追猎者塔维什】
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(
            () -> {
                switch ((int) (Math.random() * 3)) {
                    case 0 -> ownerPlayer().summon(createCard(SuperFoxSpiritWildseed.class));
                    case 1 -> ownerPlayer().summon(createCard(SuperBearSpiritWildseed.class));
                    case 2 -> ownerPlayer().summon(createCard(SuperStagSpiritWildseed.class));
                }
                switch ((int) (Math.random() * 3)) {
                    case 0 -> ownerPlayer().summon(createCard(SuperFoxSpiritWildseed.class));
                    case 1 -> ownerPlayer().summon(createCard(SuperBearSpiritWildseed.class));
                    case 2 -> ownerPlayer().summon(createCard(SuperStagSpiritWildseed.class));
                }

                Leader leader = new BeaststalkerTavishLeader();
                leader.setOwner(owner);
                leader.setInfo(info);
                leader.init();
                ownerPlayer().setLeader(leader);
            }
        ));
    }

    @Getter
    @Setter
    public static class SuperFoxSpiritWildseed extends AmuletCard {

        private CardRarity rarity = CardRarity.BRONZE;
        public Integer cost = 1;

        public String name = "强化狐灵之种";
        public String job = "猎人";
        private List<String> race = Lists.ofStr("灵种");

        public String mark = """
            亡语：召唤2个3/1且拥有【突进】的狐灵
            """;
        public String subMark = "";

        public void init() {
            setCountDown(1);
            addEffects((new Effect(this, this, EffectTiming.DeathRattle, obj ->
            {
                ownerPlayer().summon(createCard(Rexxar.FoxSpiritWildseed.FoxSpirit.class));
                ownerPlayer().summon(createCard(Rexxar.FoxSpiritWildseed.FoxSpirit.class));
            }
            )));
        }
    }

    @Getter
    @Setter
    public static class SuperBearSpiritWildseed extends AmuletCard {

        private CardRarity rarity = CardRarity.BRONZE;
        public Integer cost = 1;

        public String name = "强化熊灵之种";
        public String job = "猎人";
        private List<String> race = Lists.ofStr("灵种");

        public String mark = """
            亡语：召唤2个2/4且拥有【守护】的熊灵
            """;
        public String subMark = "";

        public void init() {
            setCountDown(2);
            addEffects((new Effect(this, this, EffectTiming.DeathRattle, obj ->
            {
                ownerPlayer().summon(createCard(Rexxar.BearSpiritWildseed.BearSpirit.class));
                ownerPlayer().summon(createCard(Rexxar.BearSpiritWildseed.BearSpirit.class));
            }
            )));
        }

    }

    @Getter
    @Setter
    public static class SuperStagSpiritWildseed extends AmuletCard {

        private CardRarity rarity = CardRarity.BRONZE;
        public Integer cost = 1;

        public String name = "强化鹿灵之种";
        public String job = "猎人";
        private List<String> race = Lists.ofStr("灵种");

        public String mark = """
            亡语：召唤2个4/3的鹿灵，对敌方主战者造成6点伤害
            """;
        public String subMark = "";

        public void init() {
            setCountDown(3);
            addEffects((new Effect(this, this, EffectTiming.DeathRattle, obj -> {
                ownerPlayer().summon(createCard(Rexxar.StagSpiritWildseed.StagSpirit.class));
                ownerPlayer().summon(createCard(Rexxar.StagSpiritWildseed.StagSpirit.class));
                info.damageEffect(this, enemyLeader(), 6);
            })));
        }

    }

}
