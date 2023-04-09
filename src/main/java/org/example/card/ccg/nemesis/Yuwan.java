package org.example.card.ccg.nemesis;

import lombok.Getter;
import lombok.Setter;
import org.example.card.*;
import org.example.card._derivant.Derivant;
import org.example.card.ccg.hunter.Rexxar;
import org.example.constant.EffectTiming;
import org.example.game.*;
import org.example.system.Database;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


@Getter
@Setter
public class Yuwan extends Leader {
    private String name = "伊昂";
    private String job = "复仇者";

    private String skillName = "虚空解析";
    private String skillMark = """
        将1张手牌加入牌堆，召唤1个解析的造物
        """;
    private int skillCost = 2;

    @Override
    public List<GameObj> targetable() {
        List<GameObj> targetable = super.targetable();
        targetable.addAll(ownerPlayer().getHand());
        return targetable;
    }

    @Override
    public void skill(GameObj target) {
        super.skill(target);
        PlayerInfo playerInfo = ownerPlayer();

        // 将1张手牌加入牌堆
        playerInfo.backToDeck((Card) target);

        // 召唤1个解析的造物
        playerInfo.summon(createCard(AnalyzingArtifact.class));

    }


    @Getter
    @Setter
    public static class AnalyzingArtifact extends FollowCard {
        private String name = "解析的造物";
        private Integer cost = 1;
        private int atk = 2;
        private int hp = 1;
        private String job = "复仇者";
        private List<String> race = Lists.ofStr("创造物");
        private String mark = """
            亡语：抽1张牌
            """;
        private String subMark = "";


        public void init() {
            setMaxHp(getHp());
            addEffects((new Effect(this, this, EffectTiming.DeathRattle, obj -> {
                ownerPlayer().draw(1);
            })));
        }
    }
    @Getter
    @Setter
    public static class AncientArtifact extends FollowCard {
        private String name = "古老的创造物";
        private Integer cost = 1;
        private int atk = 3;
        private int hp = 1;
        private String job = "复仇者";
        private List<String> race = Lists.ofStr("创造物");
        private String mark = """
            """;
        private String subMark = "";


        public void init() {
            setMaxHp(getHp());
            getKeywords().add("突进");
        }
    }

    @Getter
    @Setter
    public static class MysticArtifact extends FollowCard {
        private String name = "神秘的创造物";
        private Integer cost = 3;
        private int atk = 2;
        private int hp = 3;
        private String job = "复仇者";
        private List<String> race = Lists.ofStr("创造物");
        private String mark = """
            战吼：抽取1张卡片。
            """;
        private String subMark = "";


        public void init() {
            setMaxHp(getHp());
            getKeywords().add("守护");
            setPlay(new Play(()->{
                ownerPlayer().draw(1);
            }));
        }
    }
    @Getter
    @Setter
    public static class BifurcatingArtifact extends FollowCard {
        private String name = "增殖的创造物";
        private Integer cost = 3;
        private int atk = 2;
        private int hp = 2;
        private String job = "复仇者";
        private List<String> race = Lists.ofStr("创造物");
        private String mark = """
            战吼：召唤1个增殖的创造物到战场上。
            """;
        private String subMark = "";


        public void init() {
            setMaxHp(getHp());
//            getKeywords().add("突进"); // TODO 加强
            setPlay(new Play(()->{
                ownerPlayer().summon(createCard(BifurcatingArtifact.class));
            }));
        }
    }

    @Getter
    @Setter
    public static class RadiantArtifact extends FollowCard {
        private String name = "绚烂的创造物";
        private Integer cost = 5;
        private int atk = 4;
        private int hp = 3;
        private String job = "复仇者";
        private List<String> race = Lists.ofStr("创造物");
        private String mark = """
            亡语：搜索1张创造物卡
            """;
        private String subMark = "";


        public void init() {
            setMaxHp(getHp());
            getKeywords().add("疾驰");
            addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->
                ownerPlayer().draw(card -> card.hasRace("创造物")))));
        }
    }
    @Getter
    @Setter
    public static class BarrierArtifact extends FollowCard {
        private String name = "守御的创造物";
        private Integer cost = 5;
        private int atk = 4;
        private int hp = 6;
        private String job = "复仇者";
        private List<String> race = Lists.ofStr("创造物");
        private String mark = """
            """;
        private String subMark = "";


        public void init() {
            setMaxHp(getHp());
            getKeywords().add("剧毒");
            getKeywords().add("守护");
        }
    }
    @Getter
    @Setter
    public static class KeenedgeArtifact extends FollowCard {
        private String name = "锋锐的创造物";
        private Integer cost = 5;
        private int atk = 3;
        private int hp = 4;
        private String job = "复仇者";
        private List<String> race = Lists.ofStr("创造物");
        private String mark = """
            """;
        private String subMark = "";


        public void init() {
            setMaxHp(getHp());
            getKeywords().add("突进");
            getKeywords().add("吸血");
        }
    }
    @Getter
    @Setter
    public static class AirstrikeArtifact extends FollowCard {
        private String name = "迅袭的创造物\n";
        private Integer cost = 5;
        private int atk = 2;
        private int hp = 2;
        private String job = "复仇者";
        private List<String> race = Lists.ofStr("创造物");
        private String mark = """
            亡语：给予敌方的主战者2点伤害。
            """;
        private String subMark = "";


        public void init() {
            setMaxHp(getHp());
            getKeywords().add("疾驰");
            addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->
                info.damageEffect(this,enemyLeader(),2))));
        }
    }
    @Getter
    @Setter
    public static class ParadigmShift extends SpellCard {
        public Integer cost = 7;
        public String name = "典范转移";
        public String job = "复仇者";
        private List<String> race = Lists.ofStr("");
        public String mark = """
            无限注能（1），创造物卡：本卡费用-1
            发现其中1张并召唤：
            ‧守御的创造物
            ‧锋锐的创造物
            ‧迅袭的创造物
            """;

        public String subMark = "";


        public void init() {

            addEffects((new Effect(this,this,
                EffectTiming.Charge,obj->{
                final AreaCard areaCard = (AreaCard) obj;
                return areaCard.hasRace("创造物");
            }, obj -> addCost(-1))));

            setPlay(new Play(
                ()-> ownerPlayer().discoverCard(
                    List.of(Database.getPrototype(Yuwan.BarrierArtifact.class),
                        Database.getPrototype(Yuwan.KeenedgeArtifact.class),
                        Database.getPrototype(Yuwan.AirstrikeArtifact.class)),
                prototype -> ownerPlayer().summon((AreaCard) prototype.copyBy(ownerPlayer())))));
        }
    }
}
