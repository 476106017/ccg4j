package org.example.card.ccg.neutral.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;


@Getter
@Setter
public class MysticRing extends SpellCard {
    public Integer cost = 1;
    public String name = "神秘的戒指";
    public String job = "中立";
    private List<String> race = Lists.ofStr("财宝");
    public String mark = """
        返回1张手牌到牌堆
        抽1张牌
        """;

    public String subMark = "";
    public int target = 1;

    @Override
    public void init() {
        this.count();
    }

    public MysticRing() {
        setPlay(new Play(()->
            ownerPlayer().getHandAsGameObjBy(card -> card!=this),
            true,
            obj->{
                Card target = (Card)obj;
                ownerPlayer().backToDeck(target);
                ownerPlayer().draw(1);
            }));
    }

}
