package org.example.card.shadowverse.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;



@Getter
@Setter
public class CalamitysGenesis extends SpellCard {
    public Integer cost = 1;
    public String name = "灾祸模式";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("灾厄");
    public String mark = """
        将2张正义暴君洗入牌堆。回合结束时，搜索1张5费以上随从。
        """;

    public String subMark = "";

    public CalamitysGenesis() {

        setPlay(new Play(() -> {
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(MagisterialDreadnought.class));
                addCards.add(createCard(MagisterialDreadnought.class));
                ownerPlayer().addDeck(addCards);

                // 创建主战者回合结束效果
                ownerPlayer().getLeader().addEffect(
                    new Effect(this,null,EffectTiming.EndTurn, 1,
                        obj -> ownerPlayer().draw(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                    ), true);
        }));
    }
    @Getter
    @Setter
    public static class MagisterialDreadnought extends FollowCard {
        private Integer cost = 5;
        private String name = "正义暴君";
        private String job = "复仇者";

        private List<String> race = new ArrayList<>();
        private String mark = """
        瞬念召唤：回合开始时被破坏的5费以上随从大于5个
        入场时：召唤1个世界驱除者
        离场时：将2张幻境粉碎者洗入牌堆
        """;
        private String subMark = "";

        private int atk = 5;
        private int hp = 5;

        public MagisterialDreadnought() {
            setMaxHp(getHp());
            addEffects((new Effect(this,this, EffectTiming.Entering, obj->{
                ownerPlayer().summon(createCard(WorldEliminator.class));
            })));
            addEffects((new Effect(this,this, EffectTiming.Leaving, obj->{
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(RuinerOfEden.class));
                addCards.add(createCard(RuinerOfEden.class));
                ownerPlayer().addDeck(addCards);
            })));
            addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
                ()-> ownerPlayer().getGraveyard().stream()
                    .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                    .count() >= 5,
                ()->{})));
        }

    }

    @Getter
    @Setter
    public static class RuinerOfEden extends FollowCard {
        private Integer cost = 8;
        private String name = "幻境粉碎者";
        private String job = "复仇者";
        private List<String> race = new ArrayList<>();
        private String mark = """
        瞬念召唤：回合开始时被破坏的5费以上随从大于10个
        入场时：召唤3个世界驱除者，并赋予【剧毒】
        离场时：将1张灾祸降临洗入牌堆
        """;
        private String subMark = "";

        private int atk = 5;
        private int hp = 5;

        public RuinerOfEden() {
            setMaxHp(getHp());
            getKeywords().add("守护");
            getKeywords().add("剧毒");
            addEffects((new Effect(this,this, EffectTiming.Entering, obj->{
                ownerPlayer().summon(createCard(WorldEliminator.class,"剧毒"));
                ownerPlayer().summon(createCard(WorldEliminator.class,"剧毒"));
                ownerPlayer().summon(createCard(WorldEliminator.class,"剧毒"));
            })));
            addEffects((new Effect(this,this, EffectTiming.Leaving, obj->{
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(CalamitysEnd.class));
                ownerPlayer().addDeck(addCards);
            })));
            addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
                ()-> ownerPlayer().getGraveyard().stream()
                    .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                    .count() >= 10,
                ()->{})));
        }

    }


    @Getter
    @Setter
    public static class WorldEliminator extends FollowCard {
        private Integer cost = 5;
        private String name = "世界驱除者";
        private String job = "复仇者";

        private List<String> race = new ArrayList<>();
        private String mark = """
        亡语：使自己主战者生命最大值+2，回复2点生命
        """;
        private String subMark = "";

        private int atk = 3;
        private int hp = 3;

        public WorldEliminator() {
            setMaxHp(getHp());
            getKeywords().add("突进");
            addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
                ownerPlayer().addHpMax(2);
                ownerPlayer().heal(2);
            })));
        }

    }


    @Getter
    @Setter
    public static class CalamitysEnd extends SpellCard {
        public Integer cost = 0;
        public String name = "灾祸降临";
        public String job = "复仇者";
        private List<String> race = Lists.ofStr("终极灾厄");
        public String mark = """
        揭示:回合开始时被破坏的5费以上随从大于20个
        破坏双方场上全部随从。
        如果被破坏的5费以上我方随从小于30个，则将1张灾祸降临洗入牌堆；
        如果被破坏的5费以上我方随从大于30个，则赢得胜利
        """;

        public String subMark = "被破坏的5费以上随从：{count}个";

        public String getSubMark() {
            long count = ownerPlayer().getGraveyard().stream()
                .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                .count();
            return subMark.replaceAll("\\{count}",count+"");
        }

        public CalamitysEnd() {

            setPlay(new Play(() -> {
                destroy(enemyPlayer().getArea());
                destroy(ownerPlayer().getArea());

                long count = ownerPlayer().getGraveyard().stream()
                    .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                    .count();
                if(count < 30){
                    List<Card> addCards = new ArrayList<>();
                    addCards.add(createCard(CalamitysEnd.class));
                    ownerPlayer().addDeck(addCards);
                }else {
                    info.gameset(ownerPlayer());
                }
            }));
            addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
                ()-> ownerPlayer().getGraveyard().stream()
                    .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                    .count() >= 20,
                ()->{})));
        }


    }

}
