package org.example.card.dota.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;
import org.example.game.Play;

@Getter
@Setter
public class DivineRapier extends EquipmentCard {
    public Integer cost = 10;
    public String name = "圣剑";
    public int addAtk = 30;
    public int addHp = 0;
    public String job = "dota";
    public String mark = """
        圣剑在法师之战中，由神亲自交予反叛军之手
        """;

    public String subMark = "";

    public DivineRapier() {
        getKeywords().add("死亡掉落");
        setPlay(new Play(()->ownerPlayer().getAreaFollowsAsGameObj()));
    }
}