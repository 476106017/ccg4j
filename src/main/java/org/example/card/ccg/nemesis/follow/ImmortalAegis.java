package org.example.card.ccg.nemesis.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class ImmortalAegis extends FollowCard {
    private Integer cost = 6;
    private String name = "永恒之盾·席翁";
    private String job = "复仇者";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：增加1张费用为0的水银的断绝到手牌
        """;
    private String subMark = "";

    private int atk = 4;
    private int hp = 8;

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("无法破坏");
        getKeywords().add("效果伤害免疫");

        setPlay(new Play(() -> {
                List<Card> addCards = new ArrayList<>();
                MercurialMight mercurialMight = createCard(MercurialMight.class);
                mercurialMight.setCost(0);
                addCards.add(mercurialMight);
                ownerPlayer().addHand(addCards);
        }));
    }

    @Getter
    @Setter
    public static class MercurialMight  extends SpellCard {
        public Integer cost = 1;
        public String name = "水银的断绝";
        public String job = "复仇者";
        private List<String> race = Lists.ofStr();
        public String mark = """
        直到下个回合开始，使主战者获得效果伤害免疫
        """;

        public String subMark = "";

        public void init() {

            setPlay(new Play(() -> {
                    // 增加主战者效果
                    ownerLeader().addEffect(new Effect(
                        this, ownerLeader(), EffectTiming.BeforeDamaged, 2,
                        obj-> {
                            Damage damage = (Damage) obj;
                            if(!damage.isFromAtk()) damage.setDamage(0);}),false);
                }));
        }
    }

}
