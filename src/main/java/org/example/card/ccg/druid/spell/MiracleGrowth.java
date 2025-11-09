package org.example.card.ccg.druid.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class MiracleGrowth extends SpellCard {

   private CardRarity rarity = CardRarity.GOLD;
    public Integer cost = 8;
    public String name = "奇迹生长";
    public String job = "德鲁伊";
    private List<String> race = Lists.ofStr("自然");
    public String mark = """
        抽三张牌。召唤一个属性值等同于你的手牌数量并具有守护的植物。
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()->{
            ownerPlayer().draw(3);
            final int size = ownerPlayer().getHand().size();
            ownerPlayer().summon(createCard(KelpCreeper.class,size,size));
        }));
    }

    @Getter
    @Setter
    public static class KelpCreeper extends FollowCard {

        private CardRarity rarity = CardRarity.BRONZE;
        private String name = "海藻爬行者";
        private Integer cost = 1;
        private int atk = 1;
        private int hp = 1;
        private String job = "德鲁伊";
        private List<String> race = Lists.ofStr("植物");
        private String mark = "";
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
            getKeywords().add("守护");
        }
    }
}
