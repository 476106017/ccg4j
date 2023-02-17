package org.example.card.ccg.necromancer.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.EquipmentCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class BoneAshBibimbap extends SpellCard {
    public Integer cost = 3;
    public String name = "骨灰拌饭";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        回复3点生命值
        死灵术 6：再从墓地将不同类型卡牌各1张拿到手牌
        """;

    public String subMark = "";


    public BoneAshBibimbap() {
        setPlay(new Play(()->{
            ownerPlayer().heal(3);
            ownerPlayer().costGraveyardCountTo(6,()->{
                ownerPlayer().getGraveyardCopy().stream()
                    .filter(card -> card instanceof FollowCard).findFirst()
                        .ifPresent(card -> {
                            ownerPlayer().getGraveyard().remove(card);
                            ownerPlayer().addHand(card);
                        });
                ownerPlayer().getGraveyardCopy().stream()
                    .filter(card -> card instanceof SpellCard).findFirst()
                    .ifPresent(card -> {
                        ownerPlayer().getGraveyard().remove(card);
                        ownerPlayer().addHand(card);
                    });
                ownerPlayer().getGraveyardCopy().stream()
                    .filter(card -> card instanceof AmuletCard).findFirst()
                    .ifPresent(card -> {
                        ownerPlayer().getGraveyard().remove(card);
                        ownerPlayer().addHand(card);
                    });
                ownerPlayer().getGraveyardCopy().stream()
                    .filter(card -> card instanceof EquipmentCard).findFirst()
                    .ifPresent(card -> {
                        ownerPlayer().getGraveyard().remove(card);
                        ownerPlayer().addHand(card);
                    });
              });
        }));
    }

}
