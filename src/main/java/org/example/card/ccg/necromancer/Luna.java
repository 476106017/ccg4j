package org.example.card.ccg.necromancer;

import lombok.Getter;
import lombok.Setter;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.PlayerInfo;

import java.util.function.Consumer;


@Getter
@Setter
public class Luna extends Leader {
    private String name = "露娜·死亡骑士";
    private String job = "死灵法师";

    private String skillName = "露娜今天是巫妖王";
    private String skillMark =  """
        召唤1个具有【疾驰】的骷髅，在回合结束时死亡
        """;
    private int skillCost = 2;

    private boolean needTarget = false;

    private String overDrawMark =  """
        输掉游戏。死灵术 X：改为受到1点伤害(X是疲劳值)
        """;

    private Consumer<Integer> overDraw = integer -> {

        PlayerInfo player = ownerPlayer();
        for (Integer i = 0; i < integer; i++) {
            if(!player.costGraveyardCountTo(player.countWeary(),
                ()->info.damageEffect(this,this,1)))
                info.gameset(enemyPlayer());
        }
    };


    @Override
    public void skill(GameObj target) {
        super.skill(target);
        PlayerInfo playerInfo = ownerPlayer();
        Derivant.Skeleton skeleton = createCard(Derivant.Skeleton.class, "疾驰");
        skeleton.addEffects((new Effect(this,this, EffectTiming.EndTurn, skeleton::death)));
        ownerPlayer().summon(skeleton);
    }
}
