package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class AmbitiousGoblinMage  extends FollowCard {
    private String name = "宏愿哥布林法师";
    private Integer cost = 2;
    private int atk = 2;
    private int hp = 1;
    private String job = "中立";
    private List<String> race = Lists.ofStr("人类");
    private String mark = """
        战吼：将手牌的1张随从牌洗入牌堆，搜索1张费用低于该牌的随从牌
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(
            ()->ownerPlayer().getHandAsGameObjBy(card ->
                card instanceof FollowCard followCard && followCard!=this ),
            false,
            target->{
                if(target==null)return;
                ownerPlayer().backToDeck((FollowCard)target);
                ownerPlayer().draw(card -> card instanceof FollowCard followCard && followCard.getCost() < getCost());
            }));

    }
}