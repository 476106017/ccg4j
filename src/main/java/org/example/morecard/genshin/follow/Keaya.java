package org.example.morecard.genshin.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.morecard.genshin.system.ElementBaseFollowCard;
import org.example.morecard.genshin.system.ElementCostSpellCard;
import org.example.morecard.genshin.system.Elemental;
import org.example.morecard.genshin.system.ElementalDamage;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Keaya extends ElementBaseFollowCard {
    private String name = "凯亚";
    private int atk = 0;
    private int hp = 10;
    private String job = "原神";
    private List<String> race = Lists.ofStr("元素","单手剑");

    private Elemental element = Elemental.Cryo;
    private int burstNeedCharge = 2;

    private String mark = """
        战吼：获得1张霜袭
        元素充能（2）：获得1张凌冽轮舞
        """;
    private String subMark = "充能进度：{}/2";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    public Keaya() {
        setMaxHp(getHp());
        getKeywords().add("缴械");
        getKeywords().add("无法破坏");
        setPlay(new Play(
            ()->ownerPlayer().addHand(createCard(Frostgnaw.class))
        ));
    }

    @Override
    public void elementalBurst() {
        ownerPlayer().addHand(createCard(GlacialWaltz.class));
    }

    @Getter
    @Setter
    public static class Frostgnaw extends ElementCostSpellCard {
        public List<Elemental> elementCost = List.of(Elemental.Main, Elemental.Main, Elemental.Main);
        public String name = "霜袭";
        public String job = "原神";
        private List<String> race = Lists.ofStr("技能","元素战技");
        public String mark = """
        对敌方随从造成3+X点冰元素伤害（X是凯亚攻击力）
        """;
        public String subMark = "X等于{}";
        public String getSubMark() {
            return subMark.replaceAll("\\{}",((FollowCard)getParent()).getAtk()+"");
        }

        public Frostgnaw() {
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
                    new ElementalDamage(fromFollow,obj,3 + fromFollow.getAtk(),Elemental.Cryo).apply();
                    fromFollow.count();
                }));
        }
    }

    @Getter
    @Setter
    public static class GlacialWaltz extends ElementCostSpellCard {
        public List<Elemental> elementCost = List.of(Elemental.Main, Elemental.Main, Elemental.Main, Elemental.Main);
        public String name = "凌冽轮舞";
        public String job = "原神";
        private List<String> race = Lists.ofStr("技能","元素爆发");
        public String mark = """
        对敌方随从造成1+X点冰元素伤害（X是凯亚攻击力），主战者获得唯一效果【切换时：对随机一个敌方随从造成2点冰元素伤害（每回合仅可发动1次）】
        """;
        public String subMark = "X等于{}";
        public String getSubMark() {
            return subMark.replaceAll("\\{}",((FollowCard)getParent()).getAtk()+"");
        }

        public GlacialWaltz() {
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
                    new ElementalDamage(fromFollow,obj,1 + fromFollow.getAtk(),Elemental.Cryo).apply();

                    Leader leader = ownerLeader();
                    leader.addEffect(new Effect(this,leader, EffectTiming.BeginTurn,()->{
                        ownerPlayer().count(getName());
                    }), true);
                    leader.addEffect(new Effect(this,leader, EffectTiming.WhenSwapChara,()->{
                        if(ownerPlayer().getCount(getName()) > 0){
                            AreaCard areaRandomFollow = enemyPlayer().getAreaRandomFollow();
                            if(areaRandomFollow != null)
                                new ElementalDamage(leader, areaRandomFollow,2,Elemental.Cryo).apply();

                            ownerPlayer().clearCount(getName());
                        }
                    }), true);
                }));
        }
    }
}
