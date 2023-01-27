package org.example.card.shadowverse.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.*;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;
import static org.example.constant.CounterKey.TRANSMIGRATION_NUM;


@Getter
@Setter
public class ForestGenesis extends SpellCard {
    public Integer cost = 1;
    public String name = "森林模式";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("灾厄");
    public String mark = """
        将1张永恒树苗洗入牌堆。
        """;

    public String subMark = "";

    public ForestGenesis() {
        setPlay(new Play(() -> {
                List<Card> addCards = new ArrayList<>();
                addCards.add(createCard(EternalSeedling.class));
                ownerPlayer().addDeck(addCards);
            }));
    }

    @Getter
    @Setter
    public static class EternalSeedling extends FollowCard {

        public Integer cost = 0;

        public String name = "永恒树苗";
        public String job = "妖精";
        private List<String> race = Lists.ofStr("植物");
        public String mark = """
        瞬念召唤：回合开始时
        离场时：将1张永恒树苗洗入牌堆；如果墓地中的永恒树苗数量大于3，且场上没有永恒庭园，则召唤1个永恒庭园到场上
        轮回时：将1张永恒之花洗入牌堆
        """;
        public String subMark = "墓地中的永恒树苗数量:{count}";

        public int atk = 0;
        public int hp = 1;

        public String getSubMark() {
            long count = ownerPlayer().getGraveyard().stream()
                .filter(card -> card instanceof EternalSeedling)
                .count();

            return subMark.replaceAll("\\{count}", count+"");
        }

        public EternalSeedling() {
            setMaxHp(getHp());
            addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
                ()->true,
                ()->{}
            )));


            addEffects((new Effect(this,this, EffectTiming.Leaving,
                ()->{
                    List<Card> addCards = new ArrayList<>();
                    addCards.add(createCard(EternalSeedling.class));
                    ownerPlayer().addDeck(addCards);

                    // 墓地中的永恒树苗数量大于3，且场上没有永恒庭园
                    long count = ownerPlayer().getGraveyard().stream()
                        .filter(card -> card instanceof EternalSeedling)
                        .count();
                    if(count >= 3 && ownerPlayer().getArea().stream()
                        .filter(areaCard->areaCard instanceof EternalGarden).findAny().isEmpty()){
                        ownerPlayer().summon(createCard(EternalGarden.class));
                    }
                }
            )));

            addEffects((new Effect(this,this, EffectTiming.Transmigration,
                ()->{
                    List<Card> addCards = new ArrayList<>();
                    addCards.add(createCard(EternalBloom.class));
                    ownerPlayer().addDeck(addCards);
                }
            )));
        }

    }

    @Getter
    @Setter
    public static class EternalBloom extends FollowCard {

        public Integer cost = 0;

        public String name = "永恒之花";
        public String job = "妖精";
        private List<String> race = Lists.ofStr("植物");
        public String mark = """
        瞬念召唤：回合开始时
        离场时：摧毁我方场上所有植物，每摧毁1张，便随机破坏1张对手随从卡，并且抽1张牌
        轮回时：将1张永恒森林洗入牌堆
        突进
        """;
        public String subMark = "";

        public int atk = 0;
        public int hp = 1;

        public EternalBloom() {
            setMaxHp(getHp());
            getKeywords().add("突进");

            addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
                ()->true,
                ()->{}
            )));

            addEffects((new Effect(this,this, EffectTiming.Leaving,
                ()->{
                    List<AreaCard> plants = new ArrayList<>();
                    ownerPlayer().getArea().stream()
                        .filter(areaCard -> areaCard.getRace().contains("植物"))
                        .forEach(plants::add);
                    plants.forEach(plantCard->{
                        if(plantCard.atArea()){
                            plantCard.death();
                            List<AreaCard> oppositeArea = enemyPlayer().getArea();
                            oppositeArea.get((int) (oppositeArea.size() * Math.random())).death();
                            ownerPlayer().draw(1);
                        }
                    });
                }
            )));

            addEffects((new Effect(this,this, EffectTiming.Transmigration,
                ()->{
                    List<Card> addCards = new ArrayList<>();
                    addCards.add(createCard(EternalForest.class));
                    ownerPlayer().addDeck(addCards);
                }
            )));

        }

    }
    @Getter
    @Setter
    public static class EternalGarden extends AmuletCard {

        public Integer cost = 5;

        public String name = "永恒庭园";
        public String job = "妖精";
        private List<String> race = Lists.ofStr("庭园");
        public String mark = """
        回合结束时：回复主战者X点生命，并且轮回X：妖精随从卡（X是本回合使用的卡牌数）
        """;
        public String subMark = "X等于{}";

        public String getSubMark() {
            return subMark.replaceAll("\\{}", ownerPlayer().getCount(PLAY_NUM)+"");
        }


        public EternalGarden() {
            addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->{
                Integer x = ownerPlayer().getCount(PLAY_NUM);
                ownerPlayer().heal(x);

                ownerPlayer().transmigration(card ->
                    card instanceof FollowCard followCard && "妖精".equals(followCard.getJob()),x);
            })));
        }

    }

    @Getter
    @Setter
    public static class EternalForest extends SpellCard {
        public Integer cost = 0;
        public String name = "永恒森林";
        public String job = "妖精";
        private List<String> race = Lists.ofStr("终极灾厄");
        public String mark = """
        揭示:回合开始时
        主战者增加10点血上限。
        如果轮回数小于30个，则将1张永恒森林洗入牌堆；
        如果轮回数大于30个，则赢得胜利；
        """;

        public String subMark = "轮回数：{count}个";

        public String getSubMark() {
            return subMark.replaceAll("\\{count}",ownerPlayer().getCount(TRANSMIGRATION_NUM)+"");
        }

        public EternalForest() {

            setPlay(new Play(() -> {
                ownerPlayer().addHpMax(10);

                long count = ownerPlayer().getCount(TRANSMIGRATION_NUM);
                if(count < 30){
                    List<Card> addCards = new ArrayList<>();
                    addCards.add(createCard(EternalForest.class));
                    ownerPlayer().addDeck(addCards);
                }else {
                    info.gameset(ownerPlayer());
                }
            }));

            addEffects((new Effect(this,this, EffectTiming.InvocationBegin,
                ()->true,
                ()->{})));
        }
    }
}
