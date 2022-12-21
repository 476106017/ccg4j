package org.example.card.nemesis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.nemesis.follow.AnalyzingArtifact;
import org.example.game.GameInfo;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.PlayerInfo;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Yuwan extends Leader {
    public Yuwan(PlayerInfo playerInfo) {
        this.setPlayerInfo(playerInfo);
    }

    private String name = "伊昂";
    private String job = "复仇者";

    private String skillName = "虚空解析";
    private String skillMark =  """
        将1张手牌加入牌堆，召唤1个解析的造物
        """;
    private int skillCost = 2;
    private boolean canUseSkill = true;

    @Override
    public void skill(GameObj target) {
        super.skill(target);
        PlayerInfo playerInfo = getPlayerInfo();
        GameInfo info = playerInfo.getInfo();
        if(target instanceof Leader ||
            (target instanceof Card card && !playerInfo.getHand().contains(card))){
            info.msgTo(playerInfo.getUuid(),"指定目标不正确！");
            return;
        }

        // 将1张手牌加入牌堆
        Card card = (Card) target;
        playerInfo.getDeck().add(card);
        playerInfo.getHand().remove(card);

        // 召唤1个解析的造物
        AnalyzingArtifact analyzingArtifact = new AnalyzingArtifact();
        analyzingArtifact.setOwner(info.getTurnPlayer());
        analyzingArtifact.setInfo(info);
        playerInfo.summon(analyzingArtifact);
    }
}
