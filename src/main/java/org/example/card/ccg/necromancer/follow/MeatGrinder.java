package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card._derivant.Derivant;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class MeatGrinder extends FollowCard {
    private String name = "绞肉机";
    private Integer cost = 3;
    private int atk = 3;
    private int hp = 4;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：随机绞碎牌堆的1个随从，墓地+3
        """;
    private String subMark = "";

    public MeatGrinder() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            FollowCard deckRandomFollow = ownerPlayer().getDeckRandomFollow();
            deckRandomFollow.removeWhenNotAtArea();
            ownerPlayer().addGraveyard(deckRandomFollow);

            ownerPlayer().countToGraveyard(3);
        }));
    }
}