package org.example.card.chainsawman.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;

@Getter
@Setter
public class ChainsawMode extends EquipmentCard {
    private int apposition = 0;
    public Integer cost = 3;
    public String name = "链锯模式";
    public Integer addAtk = 2;
    public Integer addHp = 2;
    public String targetName = "链锯恶魔";
    public String job = "链锯人";
    public String mark = """
        使场上的一个链锯恶魔获得+2/+2、突进、自愈、重伤，回合结束失去此效果
        """;

    public String subMark = "";

    public ChainsawMode() {
        getKeywords().add("突进");
        getKeywords().add("自愈");
        getKeywords().add("重伤");
        setCountdown(1);
    }
}
