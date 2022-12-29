package org.example.card.chainsawman.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.EquipmentCard;

import java.util.ArrayList;

@Getter
@Setter
public class DominatePipe extends EquipmentCard {
    private int apposition = 0;
    public Integer cost = 2;
    public String name = "支配之线";
    private int countdown = 1;
    public int addAtk = 0;
    public int addHp = 0;
    public String job = "链锯人";
    public String mark = """
        装备创造者获得装备随从的控制权
        """;

    public String subMark = "";

    public DominatePipe() {
        getPlays().add(new Card.Event.Play(ArrayList::new
            , 0,gameObjs -> {}
        ));

    }
}
