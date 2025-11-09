package org.example.card.ccg.neutral;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.example.constant.CounterKey.EP_NUM;
import org.example.constant.CardRarity;


@Getter
@Setter
public class ThePlayer extends Leader {


   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "玩家";
    private String job = "中立";

    private String skillName = "进化";
    private String skillMark =  """
        使一个我方随从获得+2/+2、突进
        （先手第5回合可用，可用次数2；
        后手第4回合可用，可用次数3）
        """;
    private int skillCost = 0;

    private String mark = "最基础的玩家，有炉石传说的超抽效果和影之诗的技能";

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
        follow.upgrade();

        ownerPlayer().count(EP_NUM,-1);
        getInfo().msgToThisPlayer("你还剩下"+ownerPlayer().getCount(EP_NUM)+"个进化点");

    }

}
