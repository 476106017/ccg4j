package org.example.card.ccg.necromancer.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class PlaguedGrain extends SpellCard {
    public Integer cost = 1;
    public String name = "天灾谷物";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        墓地+3
        将4张谷物箱洗入牌堆
        """;

    public String subMark = "";


    public PlaguedGrain() {
        setPlay(new Play(()->ownerPlayer().getAreaFollowsAsGameObj(),
            true,
            obj->{
                destroy((FollowCard)obj);
                ownerPlayer().draw(2);
            }));
    }

    @Getter
    @Setter
    public static class Crate extends SpellCard {
        public Integer cost = 2;
        public String name = "谷物箱";
        public String job = "死灵术士";
        private List<String> race = Lists.ofStr();
        public String mark = """
        抽到时自动施放
        召唤1个僵尸，抽1张牌
        """;

        public String subMark = "";


        public Crate() {
            setPlay(new Play(()->{
                ownerPlayer().summon(createCard(Derivant.Zombie.class));
                ownerPlayer().draw(1);
            }));

            addEffects((new Effect(this,this, EffectTiming.WhenDrawn,this::autoPlay)));
        }

    }

}
