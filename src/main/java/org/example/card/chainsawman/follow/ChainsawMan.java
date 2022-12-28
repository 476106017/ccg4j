package org.example.card.chainsawman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.chainsawman.equipment.ChainsawMode;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ChainsawMan extends FollowCard {
    private int slot = 7;
    private int apposition = 1;
    private String name = "链锯恶魔";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 3;
    private String job = "链锯人";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        恶魔转生
        战吼：增加1张链锯形态到手牌
        击杀时：如果对象是【恶魔】，则将其净化并除外；
        如果超杀，则当前装备中的链锯形态可使用次数+1
        """;
    private String subMark = "";

    public ChainsawMan() {
        setMaxHp(getHp());
        getKeywords().add("恶魔转生");

        getPlays().add(new Card.Event.Play(ArrayList::new,0,
            gameObjs ->  ownerPlayer().addHand(createCard(ChainsawMode.class))
        ));

        getWhenKills().add(new Card.Event.WhenKill(
            followCard -> {
                if(followCard.getHp() < 0){
                    if ("链锯模式".equals(getEquipment().getName())) {
                        getEquipment().addCountdown(1);
                    }
                }
                if(followCard.getRace().contains("恶魔")){
                    followCard.purify();
                    getInfo().exile(followCard);
                }
            }
        ));
    }
}
