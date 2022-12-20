package org.example.card;

import org.example.game.GameInfo;
import org.example.game.GameObj;
import org.example.game.PlayerInfo;

import java.util.ArrayList;
import java.util.List;

public abstract class Card extends GameObj {
    public GameInfo info = null;
    public int owner = 0;

    public PlayerInfo ownerPlayer(){
        return info.getPlayerInfos()[owner];
    }
    public PlayerInfo oppositePlayer(){
        return info.getPlayerInfos()[1-owner];
    }

    public abstract String getType();
    public abstract Integer getCost();
    public abstract String getName();
    public abstract String getJob();
    public abstract String getMark();
    public abstract String getSubMark();

    public void initCounter(){}

    public Integer targetNum(){
        return 0;
    }

    public List<GameObj> targetable(){return new ArrayList<>();}

    public void play(List<GameObj> targets){
        if(ownerPlayer().getPpNum() < getCost()){
            info.msgToThisPlayer("你没有足够的PP来使用该卡牌！");
            throw new RuntimeException();
        }
        int ppNum = ownerPlayer().getPpNum() - getCost();
        ownerPlayer().setPpNum(ppNum);

        ownerPlayer().count("allCost",getCost());

        ownerPlayer().getDeck().stream().filter(card -> getCost() > card.canRust())
            .forEach(Card::afterRust);
    }

    public Integer canRust() {
        return 99;
    }

    public void afterRust(){}

}
