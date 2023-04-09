package org.example.card.ccg.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.ccg.nemesis.Yuwan;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class Focus extends SpellCard {
    public Integer cost = 0;
    public String name = "全神贯注";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("");
    public String mark = """
        给予自己的主战者「自己的回合结束时，如果自己剩余的PP为1以上，则会抽取1张卡片。
        如果为3以上，则会由原本的抽取1张转变为抽取2张，并回复自己的主战者2点生命值。
        自己的回合结束时，失去此能力效果。 （主战者可以重复叠加此效果）
        """;

    public String subMark = "";
    public void init() {

        setPlay(new Play(()->{
            // 创建主战者回合结束效果
            ownerLeader().addEffect(
                new Effect(this,null, EffectTiming.EndTurn, 1,
                    obj -> {
                        if(ownerPlayer().getPpNum()>=3){
                            ownerPlayer().draw(2);
                            ownerPlayer().heal(2);
                        }else if(ownerPlayer().getPpNum()>=1){
                            ownerPlayer().draw(1);
                        }
                    }
                ), false);
        }));
    }

}
