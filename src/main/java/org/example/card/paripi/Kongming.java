package org.example.card.paripi;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.system.util.FunctionN;

import java.util.function.Consumer;


@Getter
@Setter
public class Kongming extends Leader {
    private String name = "诸葛孔明";
    private String job = "派对咖";

    private String Mark = """
        打出战吼牌时，增加1点派对热度
        """;

    // 超抽效果
    private String overDrawMark = """
        增加与疲劳值等量的派对热度，但疲劳大于10时输掉游戏。
        """;
    private Consumer<Integer> overDraw = integer -> {
        for (Integer i = 0; i < integer; i++) {
            int weary = ownerPlayer().countWeary();
            if (weary > 10) {
                info.msg("我熬不住了...大伙儿！");
                info.gameset(enemyPlayer());
            }
            addPartyHot(ownerPlayer().getWeary());
        }
    };

    private String skillName = "切克闹！";
    private String skillMark =  """
        派对热度+3，结束回合
        """;
    private int skillCost = 2;

    private int partyHot = 0;
    public void addPartyHot(int i){
        partyHot += i;
    }
    public boolean costPartyHotTo(int cost, FunctionN function){
        if(partyHot < cost){
            info.msg(this.getName()+"没有足够的派对热度来发动派对狂欢！");
            return false;
        }
        addPartyHot(-cost);
        info.tempEffectBatch(ownerPlayer().getAreaFollowsAsGameObj(),EffectTiming.WhenCostPartyHot, cost);
        info.startEffect();
        function.apply();
        return true;
    }
    public boolean costPartyHotTo(int costMax, Consumer<Integer> consumer){
        if(partyHot == 0){
            info.msg(this.getName()+"没有足够的派对热度来发动派对狂欢！");
            return false;
        }
        int cost = Math.min(partyHot,costMax);
        addPartyHot(-cost);
        info.tempEffectBatch(ownerPlayer().getAreaFollowsAsGameObj(),EffectTiming.WhenCostPartyHot, cost);
        consumer.accept(cost);
        return true;
    }

    @Override
    public void init() {
        addEffect(new Effect(this, this, EffectTiming.WhenPlay,
        obj -> obj instanceof AreaCard areaCard && areaCard.getPlay()!=null,
        obj -> {
            info.msg("药！");
            addPartyHot(1);
        }), true);
    }
    @Override
    public void skill(GameObj target) {
        super.skill(target);
        addPartyHot(3);
        info.msg("切克闹！");
        info.endTurnOfCommand();
    }
}
