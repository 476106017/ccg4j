package org.example.card.dota.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.EquipmentCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class Roshan extends FollowCard {
    private String name = "Roshan";
    private Integer cost = 6;
    private int atk = 3;
    private int hp = 9;
    private String job = "dota";
    private List<String> race = Lists.ofStr("野怪");
    private String mark = """
        入场时：装备不朽盾
        """;
    public String subMark = "";


    public Roshan() {
        setMaxHp(getHp());
        getKeywords().add("魔法护盾");
        addEffects(new Effect(this,this,EffectTiming.Entering,()->
            this.equip(createCard(ImmortalGuard.class))
        ));
    }


    @Getter
    @Setter
    public static class ImmortalGuard extends EquipmentCard {
        public Integer cost = 2;
        public String name = "不朽之守护";
        public int addAtk = 0;
        public int addHp = 0;
        public String job = "dota";
        private List<String> race = Lists.ofStr("圣遗物");
        public String mark = """
        战吼：使装备者获得【复生】
        """;

        public String subMark = "信春哥，得永生";

        public ImmortalGuard() {
            getKeywords().add("死亡掉落");
            setPlay(new Play(
                ()->ownerPlayer().getAreaFollowsAsGameObj(),true,
                gameObj -> ((Card)gameObj).addKeyword("复生")));
        }
    }
}