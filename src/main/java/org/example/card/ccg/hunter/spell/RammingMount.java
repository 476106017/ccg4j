package org.example.card.ccg.hunter.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card._derivant.Derivant;
import org.example.card.ccg.hunter.Rexxar;
import org.example.card.ccg.hunter.follow.BattyGuest;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class RammingMount extends SpellCard {
    public Integer cost = 3;
    public String name = "山羊坐骑";
    public String job = "猎人";
    private List<String> race = Lists.ofStr();
    public String mark = """
        使1个我方随从获得+2/+2、【远程】、和【亡语：召唤1只山羊】
        """;

    public String subMark = "";


    public RammingMount() {
        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObj(), true,
            target->{
                FollowCard followCard = (FollowCard) target;
                followCard.addStatus(2,2);
                followCard.addKeyword("远程");
                followCard.addEffects((new Effect(this,followCard, EffectTiming.DeathRattle, obj->
                    followCard.ownerPlayer().summon(createCard(TavishsRam.class))
                )));
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

        public TavishsRam() {
            setMaxHp(getHp());
            getKeywords().add("远程");
        }
    }
}
