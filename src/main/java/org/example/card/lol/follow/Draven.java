package org.example.card.lol.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Draven extends FollowCard {

   private CardRarity rarity = CardRarity.SILVER;
    private String name = "德莱文";
    private Integer cost = 5;
    private int atk = 6;
    private int hp = 4;
    private String job = "英雄联盟";
    private List<String> race = Lists.ofStr();
    private String mark = """
    攻击时：如果在己方战场上的位置与召唤时相同，则造成额外2点伤害
    """;
    private String subMark = "";

    private int summonPosition;

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("自愈");
        setPlay(new Play(()->{
            summonPosition = ownerPlayer().getArea().indexOf(this);
        }));
        addEffects(new Effect(this,this, EffectTiming.WhenAttack, obj->{
            FollowCard followCard = (FollowCard) obj;
            int position = ownerPlayer().getArea().indexOf(this);
            if(summonPosition == position){
                Damage damage = (Damage) obj;
                damage.addDamage(2);
            }
        }));
    }

}
