package org.example.card.dota.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.fairy.follow.Fairy;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class Nevermore  extends FollowCard {
    private String name = "影魔";
    private Integer cost = 4;
    private int atk = 0;
    private int hp = 1;
    private String job = "dota";
    private List<String> race = Lists.ofStr("天灾军团");
    private String mark = """
        战吼：获得3张影压
        击杀时：+2/+0
        亡语：对敌方第1个位置造成影魔攻击力一半的伤害，舍弃未使用的影压
        """;
    private String subMark = "";


    public Nevermore() {
        setMaxHp(getHp());
        getKeywords().add("远程");
        setPlay(new Play(() -> {
            ownerPlayer().addHand(createCard(ShadowRazeX.class));
            ownerPlayer().addHand(createCard(ShadowRazeY.class));
            ownerPlayer().addHand(createCard(ShadowRazeZ.class));
        }));

        addEffects(new Effect(this,this, EffectTiming.WhenKill, obj-> addStatus(2,0)));
        addEffects(new Effect(this,this,EffectTiming.DeathRattle,obj->{

            List<AreaCard> area = enemyPlayer().getArea();
            int size = area.size();
            if(size<1){
                info.msg(getNameWithOwner() + "没有命中任何目标！");
                return;
            }
            AreaCard areaCard = area.get(0);
            info.msg(getNameWithOwner() + "的魂之挽歌命中了" + areaCard.getId());
            if(areaCard instanceof FollowCard followCard)
                new Damage(getParent(),followCard,getAtk()/2 ).apply();

            List<Card> abandons = ownerPlayer().getHandCopy().stream()
                .filter(card -> card.getParent() == this && card.getName().startsWith("影压")).toList();
            ownerPlayer().abandon(abandons);

        }));
    }


    @Getter
    @Setter
    public static abstract class ShadowRaze extends SpellCard {
        public Integer cost = 0;
        public String name = "影压";
        public String job = "dota";
        private List<String> race = Lists.ofStr();

    }
    @Getter
    @Setter
    public static class ShadowRazeX extends ShadowRaze {
        public String name = "影压（X）";
        public String mark = """
        影魔对敌方第1个位置造成2点伤害
        """;
        public String subMark = "敌方的第1个位置是：{}";

        public String getSubMark() {
            List<AreaCard> area = enemyPlayer().getArea();
            int size = area.size();
            if(size<1)
                return subMark.replaceAll("\\{}","什么也没有");
            return subMark.replaceAll("\\{}",area.get(0).getId());
        }

        public ShadowRazeX() {
            setPlay(new Play(()->{
                List<AreaCard> area = enemyPlayer().getArea();
                int size = area.size();
                if(size<1){
                    info.msg(getNameWithOwner() + "没有命中任何目标！");
                    return;
                }
                AreaCard areaCard = area.get(0);
                info.msg(getNameWithOwner() + "命中了" + areaCard.getId());
                if(areaCard instanceof FollowCard followCard)
                    new Damage(getParent(),followCard,2).apply();
            }));
        }
    }
    @Getter
    @Setter
    public static class ShadowRazeY extends ShadowRaze {
        public String name = "影压（Y）";
        public String mark = """
        影魔对敌方第2个位置造成2点伤害
        """;
        public String subMark = "敌方的第2个位置是：{}";

        public String getSubMark() {
            List<AreaCard> area = enemyPlayer().getArea();
            int size = area.size();
            if(size<2)
                return subMark.replaceAll("\\{}","什么也没有");
            return subMark.replaceAll("\\{}",area.get(1).getId());
        }

        public ShadowRazeY() {
            setPlay(new Play(()->{
                List<AreaCard> area = enemyPlayer().getArea();
                int size = area.size();
                if(size<2){
                    info.msg(getNameWithOwner() + "没有命中任何目标！");
                    return;
                }
                AreaCard areaCard = area.get(1);
                info.msg(getNameWithOwner() + "命中了" + areaCard.getId());
                if(areaCard instanceof FollowCard followCard)
                    new Damage(getParent(),followCard,2).apply();
            }));
        }
    }
    @Getter
    @Setter
    public static class ShadowRazeZ extends ShadowRaze {
        public String name = "影压（Z）";
        public String mark = """
        影魔对敌方第3个位置造成2点伤害
        """;
        public String subMark = "敌方的第2个位置是：{}";

        public String getSubMark() {
            List<AreaCard> area = enemyPlayer().getArea();
            int size = area.size();
            if(size<3)
                return subMark.replaceAll("\\{}","什么也没有");
            return subMark.replaceAll("\\{}",area.get(2).getId());
        }

        public ShadowRazeZ() {
            setPlay(new Play(()->{
                List<AreaCard> area = enemyPlayer().getArea();
                int size = area.size();
                if(size<3){
                    info.msg(getNameWithOwner() + "没有命中任何目标！");
                    return;
                }
                AreaCard areaCard = area.get(2);
                info.msg(getNameWithOwner() + "命中了" + areaCard.getId());
                if(areaCard instanceof FollowCard followCard)
                    new Damage(getParent(),followCard,2).apply();
            }));
        }
    }
}