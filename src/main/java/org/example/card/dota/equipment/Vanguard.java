package org.example.card.dota.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;

@Getter
@Setter
public class Vanguard extends EquipmentCard {
    public Integer cost = 3;
    public String name = "先锋盾";
    public int addAtk = 0;
    public int addHp = 2;
    public String job = "dota";
    public String mark = """
        
        """;

    public String subMark = "";

    public Vanguard() {
        getKeywords().add("护甲");
        getKeywords().add("护甲");
    }
}