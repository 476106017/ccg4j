package org.example.game;


import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class GameObj {

    private static int id_iter=10000; //共用的静态变量
    public final int id;

    protected GameInfo info = null;

    protected int owner = 0;
    public PlayerInfo ownerPlayer(){
        return info.getPlayerInfos()[owner];
    }
    public PlayerInfo enemyPlayer(){
        return info.getPlayerInfos()[1-owner];
    }

    public abstract void setName(String name);
    public abstract String getName();

    public String getNameWithOwner(){
        return ownerPlayer().getName()+"的"+getName();
    };

    public void initCounter(){}

    public GameObj() {
        id_iter++;
        id = id_iter;
    }


    public <T extends Card> T createCard(Class<T> clazz, String... keywords){
        T card = createCard(clazz);
        for (String keyword : keywords) {
            card.addKeyword(keyword);
        }
        return card;
    }
    public <T extends Card> T createCard(Class<T> clazz){
        try {
            T card = clazz.getDeclaredConstructor().newInstance();
            info.msg(getNameWithOwner()+"创造了"+card.getName());
            card.setParent(this); ;
            card.setOwner(getOwner());
            card.setInfo(getInfo());
            card.initCounter();
            return card;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void destroy(AreaCard card){destroy(List.of(card));}
    public int destroy(List<AreaCard> cards){
        List<AreaCard> cardsCopy = new ArrayList<>(cards);
        return (int) cardsCopy.stream().filter(card->card.destroyedBy(this)).count();
    }
}
