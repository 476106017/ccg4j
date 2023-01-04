package org.example.card.fairy.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.card.fairy.follow.Fairy;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Getter
@Setter
public class WoodOfBrambles extends AmuletCard {

    public Integer cost = 2;

    public String name = "荆棘之森";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("自然");
    public int countDown = 2;

    public String mark = """
        战吼：增加2张妖精到手牌
        若此卡在场上，我方全部随从拥有【交战时：对交战对象造成1点伤害】效果
        """;
    public String subMark = "";

    private Map<FollowCard, FollowCard.Event.WhenBattle> effectFollows = new HashMap<>();

    public WoodOfBrambles() {
        setPlay(new Play(()->{
            ownerPlayer().addHand(createCard(Fairy.class));
            ownerPlayer().addHand(createCard(Fairy.class));
        }));
        getEffects().add(new Effect(this,this, EffectTiming.WhenAtArea,)->
            ownerPlayer().getAreaFollowsAsFollow().forEach(followCard -> {
                FollowCard.Event.WhenBattle whenBattle = new FollowCard.Event.WhenBattle(damage -> {
                    FollowCard another = (FollowCard) damage.another(followCard);
                    another.damaged(followCard, 1);
                });
                followCard.getWhenBattles().add(whenBattle);
                effectFollows.put(followCard,whenBattle);
            })
        ));
        getEffects().add(new Effect(this,this, EffectTiming.WhenSummon,areaCard -> {
            if(areaCard instanceof FollowCard followCard){
                FollowCard.Event.WhenBattle whenBattle = new FollowCard.Event.WhenBattle(damage -> {
                    FollowCard another = (FollowCard) damage.another(followCard);
                    another.damaged(followCard, 1);
                });
                followCard.getWhenBattles().add(whenBattle);
                effectFollows.put(followCard,whenBattle);
            }
        }));
        getEffects().add(new Effect(this,this, EffectTiming.WhenNoLongerAtArea,)->
            effectFollows.forEach(((followCard, whenBattle) -> followCard.getWhenBattles().remove(whenBattle)))
        ));
    }

}
