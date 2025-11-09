package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class ccyyt extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 9;
    public String name = "吵吵演艺团";
    public String job = "圣骑士";
    private List<String> race = Lists.ofStr("机械");
    public String mark = """
        亡语：召唤3个吵吵机器人
        """;
    public String subMark = "";

    public int atk = 3;
    public int hp = 6;
    public void init() {
        setMaxHp(getHp());
        getKeywords().add("守护");
        getKeywords().add("圣盾");
        addEffects(new Effect(this,this, EffectTiming.DeathRattle, obj->{
            ownerPlayer().summon(List.of(
                createCard(ccjqr.class),createCard(ccjqr.class),createCard(ccjqr.class)));
        }));
    }

    @Getter
    @Setter
    public static class ccjqr extends FollowCard {

        private CardRarity rarity = CardRarity.BRONZE;
        public Integer cost = 2;
        public String name = "吵吵机器人";
        public String job = "中立";
        private List<String> race = Lists.ofStr("机械");
        public String mark = "";
        public String subMark = "";

        public int atk = 1;
        public int hp = 2;

        public void init() {
            setMaxHp(getHp());
            getKeywords().add("守护");
            getKeywords().add("圣盾");
        }
    }
}
