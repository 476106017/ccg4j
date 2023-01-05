package org.example.card.chainsawman.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;
import org.example.game.Play;

@Getter
@Setter
public class ChainsawMode extends EquipmentCard {
    private int apposition = 0;
    public Integer cost = 3;
    public String name = "链锯模式";
    private int countdown = 1;
    public int addAtk = 2;
    public int addHp = 2;

    public String job = "链锯人";
    public String mark = """
        使场上的一个链锯恶魔获得+2/+2、突进、自愈、重伤
        """;

    public String subMark = "";

    public ChainsawMode() {
        getKeywords().add("突进");
        getKeywords().add("自愈");
        getKeywords().add("重伤");

        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObj().stream()
                .filter(gameObj -> gameObj.getName().equals("链锯恶魔")).toList(),true,
            gameObjs -> {}));

    }

}
