package org.example.card.neutral.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.game.GameObj;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class TravelerGoblin extends FollowCard {
    public Integer cost = 1;
    public String name = "哥布林旅行家";
    public String job = "中立";
    public String race = "哥布林";
    public boolean isDash = false;
    public String mark = """
        战吼：如果是第1回合，则抽1张牌；
        如果回合数大于8，则回复8点，并获得+2/+2、突进
        """;
    public String subMark = "回合数等于{turn}";

    public String getSubMark() {
        return subMark.replaceAll("\\{turn}",info.getTurn()+"");
    }

    public int atk = 1;
    public int hp = 1;
    public int maxHp = 1;

    @Override
    public void fanfare(List<GameObj> targets) {
        info.msg(getName() + "发动战吼！");
        int turn = info.getTurn();
        if(turn ==1){
            ownerPlayer().draw(1);
        } else if (turn >= 8) {
            changeStatus(2,2);
            this.acquireDash();
        }
    }
}
