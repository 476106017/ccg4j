package org.example.card.dota.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;

import static org.example.constant.CounterKey.PLAY_NUM;

@Getter
@Setter
public class PoorMansSheild extends EquipmentCard {
    public Integer cost = 1;
    public String name = "穷鬼盾";
    public int addAtk = 0;
    public int addHp = 0;
    public String job = "dota";
    public String mark = """
        
        """;

    public String subMark = "";

    public PoorMansSheild() {
        getKeywords().add("护甲");
    }
}