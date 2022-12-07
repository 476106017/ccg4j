package org.example.card.follow;

import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.Patten;

import java.util.ArrayList;
import java.util.List;

public class Fairy extends FollowCard {
    public final String NAME = "妖精";
    public String MARK = """
        瞬念召唤：1点>0。如果分数>1则消失
        """;

    public int atk = 1;
    public int hp = 1;
    public int maxHp = 1;
    public String job = "妖精";
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
