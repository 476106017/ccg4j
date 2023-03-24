package org.example.morecard.genshin.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.morecard.genshin.system.ElementCostSpellCard;
import org.example.morecard.genshin.system.Elemental;
import org.example.morecard.genshin.system.ElementalDamage;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class ChaosMeteor extends ElementCostSpellCard {
    public List<Elemental> elementCost = List.of(Elemental.Electro, Elemental.Pydro, Elemental.Pydro);
    public String name = "混沌陨石";
    public String job = "原神";
    private List<String> race = Lists.ofStr();
    public String mark = """
    对前3个敌方随从造成4点火元素伤害
    """;
    public String subMark = "";

    public ChaosMeteor() {
        setPlay(new Play(
            ()->{
                List<FollowCard> enemyFollows = enemyPlayer().getAreaFollowsAsFollow();
                if(enemyFollows.isEmpty())return;
                List<FollowCard> subList = enemyFollows.subList(0, Math.min(enemyFollows.size(), 3));
                subList.forEach(followCard ->
                    new ElementalDamage(this,followCard,4,Elemental.Pydro).apply());
            }));
    }
}