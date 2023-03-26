package org.example.card.ccg.festival.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class ylxs extends FollowCard {
    public Integer cost = 5;
    public String name = "幽灵写手";
    public String job = "中立";
    private List<String> race = Lists.ofStr("亡灵");
    public String mark = """
        战吼：发现1张法术牌 压轴：再发现1张
        """;
    public String subMark = "";

    public int atk = 4;
    public int hp = 4;

    public ylxs() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            ownerPlayer().discoverCard(card -> card instanceof SpellCard,
                discoverCard-> {
                    ownerPlayer().addHand(discoverCard);

                    if(ownerPlayer().getPpNum()==0)
                        ownerPlayer().discoverCard(card -> card instanceof SpellCard,
                            discoverCard2-> ownerPlayer().addHand(discoverCard2));
                });
        }));
    }

}
