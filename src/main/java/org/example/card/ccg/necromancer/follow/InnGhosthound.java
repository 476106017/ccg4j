package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class InnGhosthound extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "旅馆的幽灵犬";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 1;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：如果葬送发动，则抽一张卡
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->ownerPlayer().getHandAsGameObjBy(card -> card instanceof FollowCard),
            false,
            obj->{
                if(obj==null || !ownerPlayer().burial((FollowCard) obj))return;
                ownerPlayer().draw(1);
        }));
    }
}
