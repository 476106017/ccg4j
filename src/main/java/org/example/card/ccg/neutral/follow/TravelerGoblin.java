package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;


@Getter
@Setter
public class TravelerGoblin extends FollowCard {
    public Integer cost = 1;
    public String name = "哥布林旅行家";
    public String job = "中立";
    private List<String> race = Lists.ofStr("哥布林");
    public boolean isDash = false;
    public String mark = """
        战吼：如果是第1回合，则抽1张牌；
        如果回合数大于8，则回复主战者8点生命，并获得+2/+2、突进
        """;
    public String subMark = "回合数等于{turn}";


    public String getSubMark() {
        return subMark.replaceAll("\\{turn}",info.getTurn()+"");
    }

    public int atk = 1;
    public int hp = 1;

    public TravelerGoblin() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
                int turn = info.getTurn();
                if(turn ==1){
                    ownerPlayer().draw(1);
                } else if (turn >= 8) {
                    ownerPlayer().heal(8);
                    addStatus(2,2);
                    addKeyword("突进");
                }else{
                    info.msg(getName() + "战吼后什么也没有发生！");
                }
            }));

    }

}
