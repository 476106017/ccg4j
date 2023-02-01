package org.example.card.ccg.necromancer.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class TributeSummon extends SpellCard {
    public Integer cost = 1;
    public String name = "祭品召唤";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr("邪能");
    public String mark = """
        指定我方场上1名费用小于5的随从
        指定手牌1名费用大于等于5的随从
        破坏前者，后者费用减少X（X是前者费用）
        """;

    public String subMark = "";


    public TributeSummon() {
        setPlay(new Play(()->
            List.of(ownerPlayer().getAreaFollowsAsGameObjBy(followCard -> followCard.getCost()<5),
                ownerPlayer().getHandAsGameObjBy(card -> card instanceof FollowCard && card.getCost()>=5)),
            2,true,
            objs->{
                if(objs.get(0) instanceof FollowCard followCard && objs.get(1) instanceof FollowCard followCard1){
                    followCard.destroyedBy(this);
                    followCard1.setCost(followCard1.getCost() - followCard.getCost());
                }
            }));
    }

}
