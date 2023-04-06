package org.example.card.ccg.vampire.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class ShowdownDemon extends FollowCard {
    private String name = "梭牌俏恶魔";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 2;
    private String job = "吸血鬼";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：舍弃1张自己的手牌，下个自己的回合开始时，抽取1张卡片。
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());

        setPlay(new Play(
            ()->ownerPlayer().getHandAsGameObjBy(card ->card!=this ),
            false,
            target->{
                if(target==null)return;
                ownerPlayer().abandon((Card) target);
                ownerLeader().addEffect(new Effect(this,this, EffectTiming.BeginTurn,3,
                    ()->ownerPlayer().draw(1)),false);

            }));
    }
}
