package org.example.card.ccg.neutral.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class AlteredFate extends SpellCard {
    public Integer cost = 2;
    public String name = "崭新的命运";
    public String job = "中立";
    private List<String> race = Lists.ofStr();
    public String mark = """
        舍弃全部手牌，并抽取与「舍弃的张数」等量的卡片。
        """;

    public String subMark = "";


    public AlteredFate() {
        setPlay(new Play(()->{
            final int size = ownerPlayer().getHand().size();
            ownerPlayer().abandon(ownerPlayer().getHandCopy());
            ownerPlayer().draw(size);
        }));
    }

}
