package org.example.card.paripi.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.paripi.Kongming;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;
import org.springframework.util.CollectionUtils;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class LowKeyTalentScouts extends FollowCard {

   private CardRarity rarity = CardRarity.SILVER;
    private String name = "低调的星探";
    private Integer cost = 3;
    private int atk = 2;
    private int hp = 2;
    private String job = "派对咖";
    private List<String> race = Lists.ofStr();
    private String mark = """
        回合结束时：派对狂欢 3：发动场上随机1名随从的战吼
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->{
            List<AreaCard> areaCards = info.getAreaFollowsCopy();
            if(ownerLeader() instanceof Kongming kongming)
                kongming.costPartyHotTo(3,()->{
                    if(!CollectionUtils.isEmpty(areaCards))
                        Lists.randOf(areaCards).fanfare();
                });
        })));
    }
}
