package org.example.card.deathnote.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.EquipmentCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;

import java.util.List;

@Getter
@Setter
public class DeathNote extends EquipmentCard {
    private int apposition = 3;
    public Integer cost = 4;
    public String name = "死亡笔记";
    public int addAtk = 0;
    public int addHp = 0;
    public String job = "死亡笔记";
    public String mark = """
        战吼：抉择：
        1. 什么都不做
        2. 支付我方主战者一半生命，曝光对手场上全部随从的真实名字
        回合结束时：如果装备对象未攻击，则破坏敌方所有实名随从，并使战场上的夜神月返回手牌
        """;

    public String subMark = "";

    public DeathNote() {
        getKeywords().add("游魂");
        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObj(),2,
            (choice,gameObjs) -> {
                if(choice==2){
                    ownerPlayer().getLeader().damaged(this,ownerPlayer().getHp()/2);
                    enemyPlayer().getAreaFollows().forEach(Card::exposeRealName);
                }
            }));
        addEffects((new Effect(this,this, EffectTiming.EndTurn, obj->{
            if(getTarget().getTurnAttack() < getTarget().getTurnAttackMax()){
                List<AreaCard> areaFollows = enemyPlayer().getAreaFollowsBy(Card::isRealName);
                int killNum = destroy(areaFollows);
                ownerPlayer().getAreaFollowsBy(followCard ->
                        followCard.getName().equals("夜神月"))
                    .forEach(areaCard -> {

                        areaCard.count(killNum);

                        areaCard.backToHand();
                    });
            }
        })));
    }
}