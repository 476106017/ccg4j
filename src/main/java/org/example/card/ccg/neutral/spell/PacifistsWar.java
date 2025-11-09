package org.example.card.ccg.neutral.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.game.PlayerInfo;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class PacifistsWar extends SpellCard {

   private CardRarity rarity = CardRarity.GOLD;
    public Integer cost = 1;
    public String name = "反战之战";
    public String job = "中立";
    private List<String> race = Lists.ofStr("任务");
    public String mark = """
        揭示：对战开始时，我方主战者获得【随从攻击时：输掉游戏】
        ——
        使超抽效果变成：赢得游戏胜利
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            PlayerInfo player = ownerPlayer();
            player.getLeader().setOverDraw(i-> info.gameset(player));
            player.getLeader().setOverDrawMark("赢得游戏胜利");
        }));
        addEffects(new Effect(this,this, EffectTiming.InvocationBegin,
            ()->true,
            ()->{
                Leader leader = ownerLeader();
                leader.addEffect(new Effect(this,leader, EffectTiming.WhenAttack,damage->{
                    info.gameset(enemyPlayer());
                }), true);
            }));
    }

}
