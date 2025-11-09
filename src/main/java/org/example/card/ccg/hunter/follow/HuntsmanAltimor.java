package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.ccg.hunter.BeaststalkerTavishLeader;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class HuntsmanAltimor extends FollowCard {

   private CardRarity rarity = CardRarity.BRONZE;
    private String name = "猎手阿尔迪莫";
    private Integer cost = 7;
    private int atk = 5;
    private int hp = 4;
    private String job = "猎人";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：召唤1个动物伙伴；无限注能（4）：再召唤1个；
        """;

    public String subMark = "注能次数：{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    public void init() {
        setMaxHp(getHp());
        addEffects((new Effect(this,this,
            EffectTiming.Charge, obj -> count())));
        setPlay(new Play(()-> {
            for (int j = 0; j < getCount()/4 + 1; j++) {
                switch ((int)(Math.random()*3)){
                    case 0 -> ownerPlayer().summon(createCard(BeaststalkerTavishLeader.Misha.class));
                    case 1 -> ownerPlayer().summon(createCard(BeaststalkerTavishLeader.Huffer.class));
                    case 2 -> ownerPlayer().summon(createCard(BeaststalkerTavishLeader.Leokk.class));
                }
            }

        }));
    }

}
