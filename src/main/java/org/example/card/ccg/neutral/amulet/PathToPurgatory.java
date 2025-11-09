package org.example.card.ccg.neutral.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class PathToPurgatory extends AmuletCard {


   private CardRarity rarity = CardRarity.GOLD;
    public Integer cost = 4;

    public String name = "冥府之路";
    public String job = "中立";
    private List<String> race = Lists.ofStr();

    public String mark = """
        回合结束时，墓地为30以上：给予敌方的主战者与敌方的从者全体6点伤害。
        """;
    public String subMark = "";

    List<FollowCard> effectFollows = new ArrayList<>();

    public void init() {
        addEffects((new Effect(this,this,
            EffectTiming.EndTurn, obj->ownerPlayer().getGraveyardCount()>=30,
            obj -> {
                final List<GameObj> targets = enemyPlayer().getAreaFollowsAsGameObj();
                targets.add(enemyLeader());
                info.damageMulti(this,targets,6);
            })));
    }

}
