package org.example.card.ccg.fairy.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class PhantombloomPredator extends FollowCard {

   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 2;
    public int atk = 2;
    public int hp = 2;

    public String name = "妖花捕食者";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        战吼：返回1张费用为1的随从，获得+1/+0、疾驰
        """;
    public String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObjBy(followCard -> followCard.getCost()==1),
            false,
            target->{
                if(target!=null){
                    FollowCard followCard = (FollowCard) target;
                    followCard.backToHand();
                    addStatus(1,0);
                    addKeyword("疾驰");
                }
            }));
    }
}
