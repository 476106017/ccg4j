package org.example.card.ccg.nemesis;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.ccg.nemesis.follow.AnalyzingArtifact;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.PlayerInfo;

import java.util.List;


@Getter
@Setter
public class Yuwan extends Leader {
    private String name = "伊昂";
    private String job = "复仇者";

    private String skillName = "虚空解析";
    private String skillMark =  """
        将1张手牌加入牌堆，召唤1个解析的造物
        """;
    private int skillCost = 2;

    @Override
    public List<GameObj> targetable() {
        List<GameObj> targetable = super.targetable();
        targetable.addAll(ownerPlayer().getHand());
        return targetable;
    }

    @Override
    public void skill(GameObj target) {
        super.skill(target);
        PlayerInfo playerInfo = ownerPlayer();

        // 将1张手牌加入牌堆
        playerInfo.backToDeck((Card) target);

        // 召唤1个解析的造物
        playerInfo.summon(createCard(AnalyzingArtifact.class));

    }
}
