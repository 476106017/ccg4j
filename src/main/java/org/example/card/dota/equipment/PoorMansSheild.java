package org.example.card.dota.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;
import org.example.game.Play;

@Getter
@Setter
public class PoorMansSheild extends EquipmentCard {
    public Integer cost = 1;
    public String name = "穷鬼盾";
    public transient int addAtk = 0;
    public transient int addHp = 0;
    public String job = "dota";
    public String mark = """
        
        """;

    public String subMark = "";

    public PoorMansSheild() {
        getKeywords().add("护甲");
        setPlay(new Play(()->ownerPlayer().getAreaFollowsAsGameObj()));
    }
}