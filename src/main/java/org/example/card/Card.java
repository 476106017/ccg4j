package org.example.card;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.game.GameInfo;
import org.example.game.GameObj;
import org.example.game.PlayerInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Card extends GameObj {
    public GameInfo info = null;
    public int owner = 0;

    public int needCharge = 0;

    public Card parentCard = null;

    public PlayerInfo ownerPlayer(){
        return info.getPlayerInfos()[owner];
    }
    public PlayerInfo oppositePlayer(){
        return info.getPlayerInfos()[1-owner];
    }

    public abstract String getType();
    public abstract Integer getCost();
    public abstract String getName();
    public String getNameWithOwner(){
        return ownerPlayer().getName()+"的"+getName();
    };
    public abstract String getJob();
    public abstract String getRace();
    public abstract String getMark();
    public abstract String getSubMark();
    /**
     * 回合结束的瞬召
     */
    public boolean canInvocationBegin() {
        return false;
    }

    /**
     * 回合开始的瞬召
     */
    public boolean canInvocationEnd() {
        return false;
    }

    public void afterInvocationBegin(){}

    public void afterInvocationEnd(){}


    public void initCounter(){}

    public Integer targetNum(){
        return 0;
    }

    public List<GameObj> targetable(){return new ArrayList<>();}

    public <T extends Card> T createCard(Class<T> clazz){
        try {
            T card = clazz.getDeclaredConstructor().newInstance();
            card.parentCard = this;
            card.owner = this.owner;
            card.info = this.info;
            return card;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void play(List<GameObj> targets){
        if(ownerPlayer().getPpNum() < getCost()){
            info.msgToThisPlayer("你没有足够的PP来使用该卡牌！");
            throw new RuntimeException();
        }
        info.msg(ownerPlayer().getName() + "使用了" + getName());

        int ppNum = ownerPlayer().getPpNum() - getCost();
        ownerPlayer().setPpNum(ppNum);

        ownerPlayer().count("allCost",getCost());

        ownerPlayer().getDeck().stream().filter(card -> getCost() > card.canRust())
            .forEach(Card::afterRust);
    }

    // 腐蚀
    public Integer canRust() {
        return 99;
    }

    public void afterRust(){}

    // 注能
    public void charge(){
        if(getNeedCharge() > 0){
            setNeedCharge(getNeedCharge() - 1);
            info.msg(getNameWithOwner()+"积累了1点注能！");
            if(getNeedCharge() == 0){
                info.msg(getNameWithOwner()+"注能完成！");
                afterCharge();
            }
        }
    }

    public void afterCharge(){}


}
