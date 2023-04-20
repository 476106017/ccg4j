package org.example.card.ccg.shaman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.NECROMANCY_NUM;

@Getter
@Setter
public class Shiva extends FollowCard {
    private String name = "湿婆";
    private Integer cost = 8;
    private int atk = 5;
    private int hp = 13;
    private String job = "萨满";
    private List<String> race = Lists.ofStr();
    private String mark = """
        我方洗入牌堆时：随机破坏1个敌方随从，洗入1张湿婆，抽1张牌，重复X次，然后使X+1
        战吼：将1张牌洗入牌堆
        """;
    public String subMark = "X等于{}";
    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    public void init() {
        setMaxHp(getHp());
        count();
        addEffects((new Effect(this,this, EffectTiming.WhenAddDeck, obj->{
            final Integer x = getCount();
            for (int i = 0; i < x; i++) {
                if(!atArea()) break;
                final AreaCard follow = enemyPlayer().getAreaRandomFollow();
                if(follow!=null){
                    destroy(follow);
                }
                ownerPlayer().addDeck(createCard(Shiva.class));
                ownerPlayer().draw(1);
            }
        })));
        setPlay(new Play(
            ()->ownerPlayer().getHandAsGameObjBy(card -> card!=this ),
            false,
            target->{
                if(target==null)return;
                ownerPlayer().backToDeck((FollowCard)target);
            }));
    }
}