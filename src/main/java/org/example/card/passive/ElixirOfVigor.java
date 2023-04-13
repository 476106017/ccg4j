package org.example.card.passive;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ElixirOfVigor extends SpellCard {
    public Integer cost = 0;
    public String name = "活化药剂";
    public String job = "被动";
    private List<String> race = Lists.ofStr();
    public String mark = """
        在你使用一张随从牌后，将两张它的复制洗入你的牌库，其法力值消耗为1点。
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            ownerLeader().addEffect(new Effect(this,ownerLeader(), EffectTiming.WhenPlay,
                obj->{
                    Card card = (Card) obj;
                    return card instanceof FollowCard;
                },
                obj->{
                    Card card = (Card) obj;
                    List<Card> addList = new ArrayList<>();
                    final Card copy = card.copy();
                    copy.setCost(1);
                    addList.add(copy);
                    final Card copy2 = card.copy();
                    copy2.setCost(1);
                    addList.add(copy2);

                    ownerPlayer().addDeck(addList);
                }
            ));
        }));
    }

}
