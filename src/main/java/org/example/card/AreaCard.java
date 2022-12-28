package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.game.GameObj;
import org.example.system.function.FunctionN;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public abstract class AreaCard extends Card{
    public abstract String getType();

    // region 效果列表
    private List<Event.EffectBegin> effectBegins = new ArrayList<>();
    private List<Event.EffectEnd> effectEnds = new ArrayList<>();
    private List<Event.Entering> enterings = new ArrayList<>();
    private List<Event.Leaving> leavings = new ArrayList<>();
    private List<Event.DeathRattle> deathRattles = new ArrayList<>();

    // endregion

    public boolean destroyBy(GameObj from){
        if(hasKeyword("无法破坏")) {
            info.msg(getNameWithOwner() + "无法破坏！");
            return false;
        }
        info.msg(getNameWithOwner() + "被破坏！");
        death();
        if(this instanceof FollowCard followCard && from instanceof Card card){
            if(!card.getWhenKills().isEmpty())
                info.msg(card.getNameWithOwner() + "发动击杀时效果！");
            card.getWhenKills().forEach(whenKill -> whenKill.effect().accept(followCard));
        }
        return true;
    }

    public void death(){
        info.msg(getNameWithOwner()+"被送入墓地！");

        ownerPlayer().getArea().remove(this);
        if(!getLeavings().isEmpty()){
            info.msg(getNameWithOwner() + "发动离场时效果！");
            getLeavings().forEach(leaving -> leaving.effect().apply());
        }

        if(!getDeathRattles().isEmpty()){
            info.msg(getNameWithOwner() + "发动亡语效果！");
            getDeathRattles().forEach(leaving -> leaving.effect().apply());
        }

        // region 注能
        ownerPlayer().getHand()
            .forEach(card -> {
                card.getCharges().stream()
                    .filter(charge -> charge.canBeTriggered().test(this))
                    .forEach(charge -> charge.effect().accept(this));
            });
        // endregion 注能

        ownerPlayer().getGraveyard().add(this);
        ownerPlayer().countToGraveyard(1);
    }

    public static class Event {
        /** 回合开始效果 */
        public record EffectBegin(FunctionN effect){}
        /** 回合结束效果 */
        public record EffectEnd(FunctionN effect){}
        /** 入场时 */
        public record Entering(FunctionN effect){}
        /** 离场时 */
        public record Leaving(FunctionN effect){}
        /** 亡语 */
        public record DeathRattle(FunctionN effect){}

    }
}
