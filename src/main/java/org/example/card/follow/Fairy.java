package org.example.card.follow;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.Patten;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Fairy extends FollowCard {
    public String name = "妖精";
    public String job = "妖精";
    public String mark = """
        瞬念召唤：一点>0。如果分数>1则消失
        """;
    public String subMark = "";

    public int atk = 1;
    public int hp = 1;
    public int maxHp = 1;
    {pattens.add(Patten.Ones);}

    @Override
    public boolean canSuperCantrip() {
        return Patten.Ones.getScore(info) >0;
    }

    @Override
    public void afterCantrip() {
        if(score()>1){
            info.thisPlayer().getArea().remove(this);
        }
    }
}
