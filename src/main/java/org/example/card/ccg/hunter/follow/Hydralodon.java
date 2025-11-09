package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Hydralodon extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "海卓拉顿";
    private Integer cost = 7;
    private int atk = 5;
    private int hp = 5;
    private String job = "猎人";
    private List<String> race = Lists.ofStr("野兽");
    private String mark = """
        战吼：召唤2个拥有【突进】的海卓拉顿之头
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()-> {
            ownerPlayer().summon(List.of(
                createCard(HydralodonHead.class,"突进"),
                createCard(HydralodonHead.class,"突进")));
        }));
    }
    @Getter
    @Setter
    public static class HydralodonHead extends FollowCard {

        private CardRarity rarity = CardRarity.BRONZE;
        private String name = "海卓拉顿之头";
        private Integer cost = 2;
        private int atk = 3;
        private int hp = 1;
        private String job = "猎人";
        private List<String> race = Lists.ofStr("野兽");
        private String mark = """
            亡语：如果你控制着海卓拉顿，召唤2个海卓拉顿之头
            """;
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
            addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
                if(!ownerPlayer().getAreaBy(p->p instanceof Hydralodon).isEmpty()){
                    ownerPlayer().summon(List.of(
                        createCard(HydralodonHead.class),
                        createCard(HydralodonHead.class)));
                }
            })));
        }
    }

}
