package org.example.card.ccg.warlock.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class DoomLordKazzak extends FollowCard {
    private String name = "末日领主卡扎克";
    private Integer cost = 7;
    private int atk = 7;
    private int hp = 5;
    private String job = "术士";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        战吼：如果牌堆没有卡牌，则用本局对战中的舍弃卡牌填满牌堆
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            if(ownerPlayer().getDeck().isEmpty()){
                ownerPlayer().addDeck(ownerPlayer().getAbandon().stream().toList());
            }
        }));

    }
}