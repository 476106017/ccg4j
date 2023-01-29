package org.example.card.ccg.warlock.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.ccg.fairy.follow.Fairy;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.game.PlayerInfo;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BlackDeath extends SpellCard {
    public Integer cost = 2;
    public String name = "黑死病";
    public String job = "术士";
    private List<String> race = Lists.ofStr("灾厄");
    public String mark = """
        对1名敌方随从造成2点伤害，如果该随从没有死亡，获得【亡语：对相邻随从释放黑死病】
        （该亡语不能叠加）
        """;

    public String subMark = "";


    public BlackDeath() {
        Play play = new Play(() -> enemyPlayer().getAreaFollowsAsGameObj(), true, target -> {
            FollowCard targetFollow = (FollowCard) target;

            info.damageEffect(this, targetFollow, 2);
            if (targetFollow.atArea() && !targetFollow.hasKeyword("黑死病")) {
                targetFollow.addKeyword("黑死病");
                targetFollow.addEffects(new Effect(this,targetFollow, EffectTiming.DeathRattle, obj->{
                    int leaveIndex = targetFollow.getLeaveIndex();
                    if(leaveIndex < enemyPlayer().getArea().size()){
                        AreaCard areaCard = enemyPlayer().getArea().get(leaveIndex);
                        if(areaCard instanceof FollowCard followCard){
                            List<GameObj> targets = new ArrayList<>();
                            targets.add(followCard);
                            getPlay().effect().accept(0,targets);
                        }
                    }
                    if(leaveIndex > 0){
                        AreaCard areaCard = enemyPlayer().getArea().get(leaveIndex - 1);
                        if(areaCard instanceof FollowCard followCard){
                            List<GameObj> targets = new ArrayList<>();
                            targets.add(followCard);
                            getPlay().effect().accept(0,targets);
                        }
                    }
                }));
            }
        });
        setPlay(play);
    }

}