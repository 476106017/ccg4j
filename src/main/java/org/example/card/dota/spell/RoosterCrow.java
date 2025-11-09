package org.example.card.dota.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class RoosterCrow extends SpellCard {

   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 2;
    public String name = "出兵的号角";
    public String job = "dota";
    private List<String> race = Lists.ofStr();
    public String mark = """
        敌方回合结束时，双方各召唤3个近卫军团/天灾军团
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(
            () -> {
                Leader leader = ownerLeader();
                leader.addEffect(new Effect(this,leader,EffectTiming.EnemyEndTurn,3,()->{
                    List<AreaCard> cards = new ArrayList<>();
                    cards.add(createCard(SentinelCorps.class));
                    cards.add(createCard(SentinelCorps.class));
                    cards.add(createCard(SentinelCorps.class));
                    enemyPlayer().summon(cards);

                    cards = new ArrayList<>();
                    cards.add(createCard(SentinelCorps.class));
                    cards.add(createCard(SentinelCorps.class));
                    cards.add(createCard(SentinelCorps.class));
                    ownerPlayer().summon(cards);

                }),true);
            }));
    }
    @Getter
    @Setter
    public static class SentinelCorps extends FollowCard {

        private CardRarity rarity = CardRarity.BRONZE;
        private String name = "近卫军团";
        private Integer cost = 1;
        private int atk = 1;
        private int hp = 1;
        private String job = "dota";
        private List<String> race = Lists.ofStr("精灵");
        private String mark = """
        守护远古遗迹【生命之树】的忠实士兵
        """;
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
        }
    }

    @Getter
    @Setter
    public static class ScourgeCorps extends FollowCard {

        private CardRarity rarity = CardRarity.BRONZE;
        private String name = "天灾军团";
        private Integer cost = 1;
        private int atk = 1;
        private int hp = 1;
        private String job = "dota";
        private List<String> race = Lists.ofStr("不死");
        private String mark = """
        守护远古遗迹【冰封王座】的忠实士兵
        """;
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
        }
    }
}
