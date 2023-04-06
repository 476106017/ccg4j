package org.example.card.ccg.rogue.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import java.util.function.Consumer;


@Getter
@Setter
public class GoblinHood extends FollowCard {
    private String name = "兜帽哥布林";
    private Integer cost = 3;
    private int atk = 1;
    private int hp = 5;
    private String job = "潜行者";
    private List<String> race = Lists.ofStr("哥布林");
    private String mark = """
        若此卡在场上，超抽时从敌方牌堆偷取卡牌
        """;
    private String subMark = "";

    private Consumer<Integer> oldOverDraw;
    private Consumer<Integer> newOverDraw;

    public void init() {
        setMaxHp(getHp());

        addEffects(new Effect(this,this, EffectTiming.WhenAtArea,()->{
            oldOverDraw = ownerLeader().getOverDraw();
            newOverDraw = integer -> ownerPlayer().steal(integer);
            ownerLeader().setOverDraw(newOverDraw);
        }));
        addEffects(new Effect(this,this, EffectTiming.WhenNoLongerAtArea,()->{
            if(newOverDraw == ownerLeader().getOverDraw())
                ownerLeader().setOverDraw(oldOverDraw);
        }));
    }
}
