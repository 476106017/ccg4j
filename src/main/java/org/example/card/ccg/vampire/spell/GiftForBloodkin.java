package org.example.card.ccg.vampire.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.card.ccg.hunter.follow.BattyGuest;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class GiftForBloodkin extends SpellCard {
    public Integer cost = 0;
    public String name = "给仆从的礼物";
    public String job = "吸血鬼";
    private List<String> race = Lists.ofStr();
    public String mark = """
        给予双方的主战者各1点伤害。
        于双方的战场上各召唤1只饥渴的蝙蝠。
        """;

    public String subMark = "";


    public GiftForBloodkin() {
        setPlay(new Play(()->{
                info.damageMulti(this,List.of(ownerLeader(),enemyLeader()),1);
                ownerPlayer().summon(createCard(BattyGuest.ThirstyBat.class));
                enemyPlayer().summon(createEnemyCard(BattyGuest.ThirstyBat.class));
            }
        ));
    }

}
