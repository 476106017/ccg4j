package org.example.card.dota.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;
import org.example.game.Play;

@Getter
@Setter
public class Vanguard extends EquipmentCard {
    public Integer cost = 3;
    public String name = "先锋盾";
    public transient int addAtk = 0;
    public transient int addHp = 2;
    public String job = "dota";
    public String mark = """
        
        """;

    public String subMark = "";

    public void init() {
        getKeywords().add("护甲");
        getKeywords().add("护甲");
        setPlay(new Play(()->ownerPlayer().getAreaFollowsAsGameObj()));
    }
}