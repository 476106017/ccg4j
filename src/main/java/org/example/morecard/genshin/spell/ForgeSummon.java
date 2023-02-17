package org.example.morecard.genshin.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.morecard.genshin.system.ElementCostSpellCard;
import org.example.morecard.genshin.system.Elemental;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class ForgeSummon extends ElementCostSpellCard {
    public List<Elemental> elementCost = List.of(Elemental.Cryo, Elemental.Pydro, Elemental.Pydro);
    public String name = "熔炉召唤";
    public String job = "原神";
    private List<String> race = Lists.ofStr();
    public String mark = """
    召唤1个熔炉精灵
    """;
    public String subMark = "";

    public ForgeSummon() {
        setPlay(new Play(
            ()->{
                ownerPlayer().summon(createCard(ForgeSpirit.class));
            }));
    }

    @Getter
    @Setter
    public static class ForgeSpirit extends FollowCard {
        private String name = "熔炉精灵";
        private Integer cost = 2;
        private int atk = 2;
        private int hp = 2;
        private String job = "原神";
        private List<String> race = Lists.ofStr("召唤物");
        private String mark = """
        攻击时：移除目标1层格挡和1层护甲
        """;
        private String subMark = "";

        public ForgeSpirit() {
            setMaxHp(getHp());
            getKeywords().add("突进");

            addEffects(new Effect(this,this, EffectTiming.WhenAttack, obj->{
                Damage damage = (Damage) obj;
                if(damage.getTo() instanceof FollowCard toFollow){
                    toFollow.removeKeyword("格挡");
                    toFollow.removeKeyword("护甲");
                }
            }));

        }
    }
}