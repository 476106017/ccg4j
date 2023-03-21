package org.example.card.ccg.sts;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.example.constant.CounterKey.EP_NUM;


@Getter
@Setter
public class Intruder extends Leader {

    private String name = "入侵者";
    private String job = "杀戮尖塔";

    private String Mark = """
        回合开始时：再抽4张牌,最大pp值变为3
        回合结束时：将剩余手牌放置于牌堆底部
        """;
    private String skillName = "休息处";
    private String skillMark =  """
        选择一张手牌，如果可以升级，则升级；
        否则回复主战者30%的hp
        """;
    private int skillCost = 3;

    private String overDrawMark =  """
        对自己造成疲劳伤害
        """;

    private Consumer<Integer> overDraw = integer -> {
        for (int i = 0; i < integer; i++) {
            ownerPlayer().wearyDamaged();
        }
    };

    @Override
    public void init() {
        if (ownerPlayer().isInitative()) {
            ownerPlayer().count(EP_NUM,2);
        }else {
            ownerPlayer().count(EP_NUM,3);
        }
    }

    @Override
    public List<GameObj> targetable() {
        if(ownerPlayer().getCount(EP_NUM) == 0
            || (ownerPlayer().isInitative() && getInfo().getTurn()<5)
            || (!ownerPlayer().isInitative() && getInfo().getTurn()<4)){
            return new ArrayList<>();
        }

        return ownerPlayer().getAreaFollowsAsGameObj();
    }

    @Override
    public void skill(GameObj target) {
        super.skill(target);

        FollowCard follow = (FollowCard) target;
        follow.addStatus(2,2);
        follow.addKeyword("突进");

        ownerPlayer().count(EP_NUM,-1);
        getInfo().msgToThisPlayer("你还剩下"+ownerPlayer().getCount(EP_NUM)+"个进化点");

    }

}
