package org.example.card;

import org.example.constant.CardType;
import org.example.constant.Patten;
import org.example.game.GameInfo;
import org.example.game.GameObj;

import java.util.ArrayList;
import java.util.List;

public abstract class Card extends GameObj {
    public GameInfo info = null;
    public int owner = 0;

    public abstract String getType();
    public abstract String getName();
    public abstract String getJob();
    public abstract String getMark();
    public abstract String getSubMark();

    public int target = 0;

    public List<Patten> pattens = new ArrayList<>();


    public void initCounter(){};

    public List<GameObj> targetable(){return new ArrayList<>();};

    public void play(List<GameObj> targets){};

    public boolean canCantrip() {
        return false;
    }

    public boolean canSuperCantrip() {
        return false;
    }

    public void afterCantrip(){};
    public int score(){
        int score = 0;
        for (Patten patten : pattens) {
            score = Math.max(patten.getScore(info), score);
        }
        return score;
    }
}
