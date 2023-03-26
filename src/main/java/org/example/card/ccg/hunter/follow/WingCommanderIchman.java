package org.example.card.ccg.hunter.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.FunctionN;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class WingCommanderIchman extends FollowCard {
    private String name = "空军指挥官艾克曼";
    private Integer cost = 9;
    private int atk = 5;
    private int hp = 4;
    private String job = "猎人";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：招募1个野兽并拥有【突进】，它在击杀时会重复此效果
        """;
    private String subMark = "";
    private transient FunctionN effect = ()->{};

    public WingCommanderIchman() {
        setMaxHp(getHp());
        effect = ()->{
            ownerPlayer().hire(card -> card instanceof FollowCard && card.hasRace("野兽"),
                areaCard -> {
                    areaCard.addKeyword("突进");
                    areaCard.addEffects((new Effect(this,this,
                        EffectTiming.WhenKill, obj -> effect.apply())));
                });
        };
        setPlay(new Play(()-> {
            effect.apply();
        }));
    }

}