package org.example.card.genshin.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.genshin.system.ElementBaseFollowCard;
import org.example.card.genshin.system.ElementCostSpellCard;
import org.example.card.genshin.system.Elemental;
import org.example.card.genshin.system.ElementalDamage;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Sucrose extends ElementBaseFollowCard {
    private String name = "砂糖";
    private int atk = 0;
    private int hp = 10;
    private String job = "原神";
    private List<String> race = Lists.ofStr("元素","法器");

    private Elemental element = Elemental.Anemo;
    private int burstNeedCharge = 2;

    private String mark = """
        战吼：获得1张风灵作成6308
        元素充能（2）：获得1张禁·风灵作成75同构2型
        """;
    private String subMark = "充能进度：{}/2";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    public Sucrose() {
        setMaxHp(getHp());
        getKeywords().add("缴械");
        getKeywords().add("无法破坏");
        setPlay(new Play(
            ()->ownerPlayer().addHand(createCard(AnemoHypostasisCreation6308.class))
        ));
    }

    @Override
    public void elementalBurst() {
        ownerPlayer().addHand(createCard(ForbiddenCreationIsomer75.class));
    }

    @Getter
    @Setter
    public static class AnemoHypostasisCreation6308 extends ElementCostSpellCard {
        public List<Elemental> elementCost = List.of(Elemental.Main, Elemental.Main, Elemental.Main);
        public String name = "风灵作成6308";
        public String job = "原神";
        private List<String> race = Lists.ofStr("技能","元素战技");
        public String mark = """
        对敌方随从造成3+X点风元素伤害（X是砂糖攻击力）
        如果目标没有阵亡且拥有【守护】，则将守护转移到敌方场上任意目标身上
        """;
        public String subMark = "X等于{}";
        public String getSubMark() {
            return subMark.replaceAll("\\{}",((FollowCard)getParent()).getAtk()+"");
        }

        public AnemoHypostasisCreation6308() {
            setPlay(new Play(
                ()->{
                    List<GameObj> enemyTargets = new ArrayList<>();
                    enemyTargets.add(info.oppositePlayer().getLeader());
                    enemyTargets.addAll(info.oppositePlayer().getAreaFollows(hasKeyword("无视守护")));
                    return enemyTargets;
                },
               true,
                obj->{
                    ElementBaseFollowCard fromFollow = (ElementBaseFollowCard)getParent();
                    new ElementalDamage(fromFollow,obj,3 + fromFollow.getAtk(),Elemental.Anemo).apply();
                    if(obj instanceof FollowCard toFollow && toFollow.atArea() && toFollow.hasKeyword("守护")){
                        toFollow.removeKeyword("守护");
                        enemyPlayer().getAreaRandomFollow().addKeyword("守护");
                    }
                    fromFollow.count();
                }));
        }
    }

    @Getter
    @Setter
    public static class ForbiddenCreationIsomer75 extends ElementCostSpellCard {
        public List<Elemental> elementCost = List.of(Elemental.Main, Elemental.Main, Elemental.Main);
        public String name = "禁·风灵作成75同构2型";
        public String job = "原神";
        private List<String> race = Lists.ofStr("技能","元素爆发");
        public String mark = """
        对敌方随从造成1点风元素伤害，召唤1个大型风灵，并增加其X回合倒数（X是砂糖攻击力）
        """;
        public String subMark = "X等于{}";
        public String getSubMark() {
            return subMark.replaceAll("\\{}",((FollowCard)getParent()).getAtk()+"");
        }

        public ForbiddenCreationIsomer75() {
            setPlay(new Play(
                ()->{
                    List<GameObj> enemyTargets = new ArrayList<>();
                    enemyTargets.add(info.oppositePlayer().getLeader());
                    enemyTargets.addAll(info.oppositePlayer().getAreaFollows(hasKeyword("无视守护")));
                    return enemyTargets;
                },
                true,
                obj->{
                    ElementBaseFollowCard fromFollow = (ElementBaseFollowCard)getParent();
                    new ElementalDamage(fromFollow,obj,1 + fromFollow.getAtk(),Elemental.Anemo).apply();


                }));
        }

        @Getter
        @Setter
        public static class LargeWindSpirit extends AmuletCard {

            public Integer cost = 4;

            public String name = "大型风灵";
            public String job = "原神";
            private List<String> race = Lists.ofStr("召唤物");
            public int countDown = 3;

            private Elemental damageType = Elemental.Anemo;

            public String mark = """
                回合结束时：对随机1个敌方随从造成2点风元素伤害（守护随从优先）
                当该伤害触发扩散时，伤害的元素类型变为被扩散的元素
                """;
            public String subMark = "当前伤害类型：{}";
            public String getSubMark() {
                return subMark.replaceAll("\\{}",getDamageType().getStr());
            }

            public LargeWindSpirit() {
                addEffects(new Effect(this,this,EffectTiming.EndTurn,()->{
                    AreaCard areaRandomFollow = enemyPlayer().getAreaRandomGuardFollow();
                    if(areaRandomFollow == null)
                        areaRandomFollow = enemyPlayer().getAreaRandomFollow();

                    if(areaRandomFollow!=null){
                        // 如果伤害对象身上挂了可扩散元素，就先记录下来
                        Elemental tempElemental = null;
                        if(damageType == Elemental.Anemo){
                            Elemental elementalCling = areaRandomFollow.getElementalCling();
                            if(elementalCling.isActive())
                                tempElemental =  elementalCling;
                        }

                        new ElementalDamage(this, areaRandomFollow,2,damageType).apply();
                        if(tempElemental!=null)
                            damageType = tempElemental;
                    }
                }));
            }
        }

    }
}
