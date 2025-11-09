package org.example.card.paripi.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class DiscoZombie extends FollowCard {

   private CardRarity rarity = CardRarity.SILVER;
    private String name = "舞王僵尸";
    private Integer cost = 3;
    private int atk = 3;
    private int hp = 3;
    private String job = "派对咖";
    private List<String> race = Lists.ofStr("不死生物");
    private String mark = """
       战吼：如果战场没有牌，则召唤4只具有【缴械】的僵尸，并使全体随从获得+1/+0
       亡语：除外己方所有【僵尸】
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            if(ownerPlayer().getArea().isEmpty()){
                List<AreaCard> zombies = new ArrayList<>();
                zombies.add(createCard(Derivant.Zombie.class,"缴械"));
                zombies.add(createCard(Derivant.Zombie.class,"缴械"));
                zombies.add(createCard(Derivant.Zombie.class,"缴械"));
                zombies.add(createCard(Derivant.Zombie.class,"缴械"));
                ownerPlayer().summon(zombies);

                ownerPlayer().getAreaFollowsAsFollow().forEach(followCard -> followCard.addStatus(1,0));
            }
            addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj->{
                List<Card> zombies = ownerPlayer()
                    .getAreaFollowsAsCardBy(followCard -> "僵尸".equals(followCard.getName()));
                info.exile(zombies);
            })));
        }));

    }
}
