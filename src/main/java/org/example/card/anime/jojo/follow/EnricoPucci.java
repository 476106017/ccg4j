package org.example.card.anime.jojo.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class EnricoPucci extends FollowCard {
    private String name = "普奇·白蛇";
    private Integer cost = 3;
    private int atk = 2;
    private int hp = 5;
    private String job = "jojo";
    private List<String> race = Lists.ofStr("替身使者");
    private String mark = """
        超杀：将被击杀随从制作成【DISC】
        抽牌时：如果抽到的是乔斯达家族随从卡，则变身成【普奇·新月】
        回合开始时：变身成【普奇·新月】
        """;
    private String subMark = "";

    public EnricoPucci() {
        setMaxHp(getHp());
        getKeywords().add("突进");
        setPlay(new Play(()->
            info.msg("普奇神父：2,3,5,7,11,13,17...")
        ));
        addEffects(new Effect(this,this, EffectTiming.WhenKill,
            obj -> ((FollowCard) obj).getHp() < 0,
            obj -> {
                FollowCard follow = (FollowCard)obj;
                follow.setName(follow.getName() + "的DISC");
                follow.setOwner(getOwner());
                addKeyword("被控制");
        }));
        addEffects(new Effect(this,this, EffectTiming.WhenDraw,obj->{
            List<Card> cards = (List<Card>) obj;
            if(cards.stream()
                .anyMatch(card -> card.hasRace("乔斯达家族"))){
                info.transform(this,createCard(EnricoPucciCMoon.class));
            }
        }));
        addEffects(new Effect(this,this, EffectTiming.BeginTurn,obj->{
            info.transform(this,createCard(EnricoPucciCMoon.class));
        }));

    }


    @Getter
    @Setter
    public static class EnricoPucciCMoon extends FollowCard {
        private String name = "普奇·新月";
        private Integer cost = 3;
        private int atk = 0;
        private int hp = 11;
        private String job = "jojo";
        private List<String> race = Lists.ofStr("替身使者");
        private String mark = """
            回合结束时：将敌方战场最后排的1张牌洗入牌堆
            抽牌时：如果抽到的是乔斯达家族随从卡，则变身成【普奇·天堂制造】
            回合开始时：变身成【普奇·天堂制造】
            """;
        private String subMark = "";

        public EnricoPucciCMoon() {
            setMaxHp(getHp());
            getKeywords().add("剧毒");
            setPlay(new Play(()->
                info.msg("普奇神父：你相信引力吗？")
            ));
            addEffects(new Effect(this,this, EffectTiming.EndTurn,obj->{
                List<AreaCard> area = enemyPlayer().getArea();
                if(!area.isEmpty()){
                    AreaCard areaCard = area.get(area.size() - 1);
                    area.remove(areaCard);
                    enemyPlayer().addDeck(areaCard);
                }

            }));

            addEffects(new Effect(this,this, EffectTiming.WhenDraw,obj->{
                List<Card> cards = (List<Card>) obj;
                if(cards.stream()
                    .anyMatch(card -> card.hasRace("乔斯达家族"))){
                    info.transform(this,createCard(EnricoPucciMadeInHeaven.class));
                }
            }));
            addEffects(new Effect(this,this, EffectTiming.BeginTurn,obj->{
                info.transform(this,createCard(EnricoPucciMadeInHeaven.class));
            }));
        }
    }
    @Getter
    @Setter
    public static class EnricoPucciMadeInHeaven extends FollowCard {
        private String name = "普奇·天堂制造";
        private Integer cost = 3;
        private int atk = 2;
        private int hp = 13;
        private String job = "jojo";
        private List<String> race = Lists.ofStr("替身使者");
        private String mark = """
            受伤前：有17%的几率闪避受到的普攻伤害(包含反击伤害)
            —————————————
            回合结束时，发动以下全部效果：
            1.双方PP最大值减半
            2.普奇的回合可攻击数+1
            3.闪避几率+12%
            4.如果此时敌方PP最大值为1，则发动以下全部效果：
            (1)重启游戏并召唤此卡
            (2)失去回合结束时效果并获得【魔法免疫】
            """;
        private String subMark = "当前回合可攻击数：{T}\t当前闪避几率：{R}";

        private Integer rate = 17;
        private transient Effect endTurnEffect;

        public String getSubMark() {
            return subMark.replaceAll("\\{T}", getTurnAttackMax()+"")
                .replaceAll("\\{R}", getRate()+"%");
        }

        public EnricoPucciMadeInHeaven() {
            setMaxHp(getHp());
            getKeywords().add("无法破坏");
            setPlay(new Play(() ->
                info.msg("普奇神父：最后说一次，时间要加速了")
            ));
            addEffects(new Effect(this,this,EffectTiming.BeforeDamaged,obj->{
                if(Math.random()*100 > getRate())return;

                getInfo().msg("没有命中普奇神父！");
                Damage damage = (Damage) obj;
                damage.setMiss(true);
            }));
            endTurnEffect = new Effect(this, this, EffectTiming.EndTurn, obj -> {
                enemyPlayer().setPpMax(enemyPlayer().getPpMax() / 2);
                ownerPlayer().setPpMax(enemyPlayer().getPpMax() / 2);
                setTurnAttackMax(getTurnAttackMax() + 1);
                setRate(getRate() + 12);
                getInfo().msg("敌方PP最大值变成了" + enemyPlayer().getPpMax());
                getInfo().msg("我方PP最大值变成了" + ownerPlayer().getPpMax());
                getInfo().msg("普奇的回合可攻击数变成了" + getTurnAttackMax());
                getInfo().msg("普奇的闪避几率变成了" + getRate() + "%");

                if (enemyPlayer().getPpMax() == 1) {
                    info.msg("普奇神父：Made in Heaven!");
                    info.resetGame();
                    ownerPlayer().summon(this);
                    setMark("受伤前：有"+getRate()+"%的几率闪避受到的普攻伤害(包含反击伤害)");
                    info.msg("""
                    普奇神父：这些都不是用头脑或肉体去记忆，而是用精神去体验过后所得到的认知！
                    那就是「幸福」的真谛！不单单只有一人，而是全体人类都能对未来有所「觉悟」！
                    """);
                    getEffects().remove(endTurnEffect);
                    removeKeyword("无法破坏");
                    addKeyword("魔法免疫");
                }
            });
            addEffects(endTurnEffect);
        }
    }

}