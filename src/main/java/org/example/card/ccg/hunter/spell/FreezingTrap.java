package org.example.card.ccg.hunter.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;
import org.example.system.util.Msg;

import java.util.List;


@Getter
@Setter
public class FreezingTrap extends SpellCard {
    public Integer cost = 2;
    public String name = "冰冻陷阱";
    public String job = "猎人";
    private List<String> race = Lists.ofStr("冰霜");
    public String mark = """
        使一个未攻击的敌方随从移回拥有者手牌
        """;

    public String subMark = "";


    public void init() {
        getKeywords().add("速攻");
        setPlay(new Play(
            ()-> enemyPlayer().getAreaFollowsAsGameObjBy(followCard -> followCard.getTurnAttack()==0), true,
            target->{
                FollowCard followCard = (FollowCard) target;
                if(followCard.getTurnAttack()==0){
                    followCard.backToHand();
                }else {
                    Msg.send(ownerPlayer().getSession(), "指定的随从已经攻击了，所以什么也没有发生");
                }
            }));
    }

    @Getter
    @Setter
    public static class TavishsRam extends FollowCard {
        private String name = "塔维什的山羊";
        private Integer cost = 2;
        private int atk = 2;
        private int hp = 2;
        private String job = "猎人";
        private List<String> race = Lists.ofStr("野兽");
        private String mark = "";
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
            getKeywords().add("远程");
        }
    }
}
