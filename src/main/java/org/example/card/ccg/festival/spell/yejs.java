package org.example.card.ccg.festival.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class yejs extends SpellCard {
    public Integer cost = 3;
    public String name = "悦耳金属";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        随机使手牌中4张随从获得+2/+2（每回合切换）
        """;

    public String subMark = "";

    public void init() {
        addEffects(new Effect(this,this, EffectTiming.BeginTurnAtHand, obj->{
            info.exile(this);
            ownerPlayer().addHand(createCard(cejs.class));
        }));

        setPlay(new Play(
            () -> {
                Lists.randOf(ownerPlayer().getHandFollows(),4)
                    .forEach(followCard -> {
                        followCard.addStatus(2,2);
                });
            }));
    }

    @Getter
    @Setter
    public static class cejs extends SpellCard {
        public Integer cost = 3;
        public String name = "刺耳金属";
        public String job = "死灵术士";
        private List<String> race = Lists.ofStr();
        public String mark = """
        随机使手牌中2张随从获得+4/+4（每回合切换）
        """;

        public String subMark = "";

        public void init() {
            addEffects(new Effect(this,this, EffectTiming.BeginTurnAtHand, obj->{
                info.exile(this);
                ownerPlayer().addHand(createCard(yejs.class));
            }));

            setPlay(new Play(
                () -> {
                    Lists.randOf(ownerPlayer().getHandFollows(),2)
                        .forEach(followCard -> {
                            followCard.addStatus(4,4);
                        });
                }));
        }

    }
}
