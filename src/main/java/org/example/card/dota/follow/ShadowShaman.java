package org.example.card.dota.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.*;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class ShadowShaman extends FollowCard {
    private String name = "暗影萨满";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "dota";
    private List<String> race = Lists.ofStr("近卫军团","英雄");
    private String mark = """
        战吼：获得叉形闪电、妖术、枷锁、蛇群守卫各一张
        """;
    private String subMark = "";


    public ShadowShaman() {
        setMaxHp(getHp());
        setPlay(new Play(() -> {
            ownerPlayer().addHand(createCard(ForkedLightning.class));
            ownerPlayer().addHand(createCard(Voodoo.class));
            ownerPlayer().addHand(createCard(Shackle.class));
        }));
    }

    @Getter
    @Setter
    public static class ForkedLightning extends SpellCard {
        public Integer cost = 3;
        public String name = "叉形闪电";
        public String job = "dota";
        private List<String> race = Lists.ofStr("技能");
        public String mark = """
        对敌方前3个位置分别造成3点伤害
        """;
        public String subMark = "";

        public ForkedLightning() {
            setPlay(new Play(()->{
                List<AreaCard> area = enemyPlayer().getArea();
                if(area.size()>0 && area.get(0) instanceof FollowCard followCard){
                    info.msg(getNameWithOwner() + "命中了" + followCard.getId());
                    new Damage(getParent(),followCard,3).apply();
                }
                if(area.size()>1 && area.get(1) instanceof FollowCard followCard){
                    info.msg(getNameWithOwner() + "命中了" + followCard.getId());
                    new Damage(getParent(),followCard,3).apply();
                }
                if(area.size()>2 && area.get(2) instanceof FollowCard followCard){
                    info.msg(getNameWithOwner() + "命中了" + followCard.getId());
                    new Damage(getParent(),followCard,3).apply();
                }
            }));
        }
    }
    @Getter
    @Setter
    public static class Voodoo extends SpellCard {
        public Integer cost = 4;
        public String name = "妖术";
        public String job = "dota";
        private List<String> race = Lists.ofStr("技能");
        public String mark = """
        到下回合开始前，将1个敌方随从变成小动物
        """;
        public String subMark = "";

        public FollowCard target = null;
        public Sheep sheep = null;

        public Voodoo() {
            setPlay(new Play(()->enemyPlayer().getAreaFollowsAsGameObj(),true,obj->{
                if(obj instanceof FollowCard followCard){
                    Sheep sheep = createEnemyCard(Sheep.class);
                    sheep.setHp(followCard.getHp());
                    sheep.setMaxHp(followCard.getMaxHp());
                    target = followCard;
                    this.sheep = sheep;
                    getInfo().transform(followCard,sheep);

                    Leader leader = ownerLeader();
                    leader.addEffect(new Effect(this,leader,EffectTiming.BeginTurn,3,()->{
                        getInfo().transform(this.sheep,target);
                    }), false);
                }
            }));
        }
        @Getter
        @Setter
        public static class Sheep extends FollowCard {
            private String name = "绵羊";
            private Integer cost = 1;
            private String job = "dota";
            private List<String> race = Lists.ofStr("动物");
            private String mark = """
            看起来人畜无害的样子
            """;
            private String subMark = "";
        }
    }
    @Getter
    @Setter
    public static class Shackle extends SpellCard {
        public Integer cost = 1;
        public String name = "枷锁";
        public String job = "dota";
        private List<String> race = Lists.ofStr("技能");
        public String mark = """
            暗影萨满使用魔法绑住1个敌方随从
            暗影萨满获得【缴械】和【回合结束时：眩晕该随从，并对该随从造成1点伤害】持续3回合、或者枷锁消失
            """;
        public String subMark = "";

        public int startTurn = 0;

        public FollowCard target;
        public Effect effect;
        public Effect targetEffect;

        public Shackle() {
            setPlay(new Play(()->enemyPlayer().getAreaFollowsAsGameObj(),true,obj->{
                if(obj instanceof FollowCard followCard){
                    Card shadowShaman = (Card) getParent();
                    shadowShaman.addKeyword("缴械");
                    target = followCard;
                    startTurn = info.getTurn();

                    // 增加回合结束效果
                    effect = new Effect(this,getParent(),EffectTiming.EndTurn,()->{
                        if(target!=null){
                            target.addKeyword("眩晕");
                            new Damage(getParent(),target,1).apply();
                            if(info.getTurn() >= startTurn + 2){
                                shadowShaman.removeKeyword("缴械");
                                getParent().getEffects().remove(effect);
                                if(target!=null)
                                    target.getEffects().remove(targetEffect);
                                target = null;
                                effect = null;
                                targetEffect = null;
                            }
                        }
                    });
                    getParent().addEffects(effect);

                    // 增加目标不在场时效果，清理本技能效果
                    targetEffect = new Effect(this,target,EffectTiming.WhenNoLongerAtArea,()->{
                        if(effect!=null){
                            shadowShaman.removeKeyword("缴械");
                            getParent().getEffects().remove(effect);
                            target = null;
                            effect = null;
                        }
                    });
                    target.addEffects(targetEffect);
                }
            }));
        }
    }
    @Getter
    @Setter
    public static class MassSerpentWard extends SpellCard {
        public Integer cost = 6;
        public String name = "蛇群守卫";
        public String job = "dota";
        private List<String> race = Lists.ofStr("技能");
        public String mark = """
            在敌方战场上召唤3只蛇棒
            """;
        public String subMark = "";

        public int startTurn = 0;

        public FollowCard target;
        public Effect effect;
        public Effect targetEffect;

        public MassSerpentWard() {
            setPlay(new Play(() -> {
                enemyPlayer().summon(createEnemyCard(SerpentBar.class));
                enemyPlayer().summon(createEnemyCard(SerpentBar.class));
                enemyPlayer().summon(createEnemyCard(SerpentBar.class));
            }));
        }
        @Getter
        @Setter
        public static class SerpentBar extends AmuletCard {

            public Integer cost = 1;

            public String name = "蛇棒";
            public String job = "dota";
            private List<String> race = Lists.ofStr();
            public int countDown = 2;

            public String mark = """
            敌方回合结束时：对我方主战者造成1点伤害
            """;
            public String subMark = "";

            public SerpentBar() {
                addEffects((new Effect(this,this, EffectTiming.EnemyEndTurn, obj->
                    new Damage(this,ownerLeader(),1).apply()
                )));
            }
        }
    }
}