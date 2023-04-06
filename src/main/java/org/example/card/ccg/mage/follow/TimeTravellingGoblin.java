package org.example.card.ccg.mage.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class TimeTravellingGoblin extends FollowCard {
    private String name = "时间旅行的地精";
    private Integer cost = 3;
    private int atk = 2;
    private int hp = 2;
    private String job = "法师";
    private List<String> race = Lists.ofStr("哥布林");
    private String mark = """
       战吼：如果牌堆没有牌，则轮回1张法师法术牌。抽1张牌。
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            if(ownerPlayer().getDeck().isEmpty()){
                ownerPlayer().transmigration(card ->
                    card instanceof SpellCard spellCard && "法师".equals(spellCard.getJob()),1);
            }
        }));

    }
}