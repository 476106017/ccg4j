package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import java.util.Objects;
import org.example.constant.CardRarity;


@Getter
@Setter
public class jpzr extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "计拍侏儒";
    public String job = "中立";
    private List<String> race = Lists.ofStr("机械");
    public String mark = """
        每当打出一张费用为（X）的卡牌，抽一张费用为（X+1）的卡牌，然后调高
        """;
    public String subMark = "X等于{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    public int atk = 2;
    public int hp = 4;

    public void init() {
        setMaxHp(getHp());
        count();
        addEffects((new Effect(this,this, EffectTiming.WhenPlay,
            o->{
                Card card = (Card) o;
                return Objects.equals(card.getCost(), getCount());
            },
            obj->{
                ownerPlayer().draw(card -> Objects.equals(card.getCost(), getCount()));
                count();
            })));
    }

}
