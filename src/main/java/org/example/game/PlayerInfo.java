package org.example.game;

import lombok.Data;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.nemesis.Yuwan;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Data
public class PlayerInfo {
    GameInfo info;

    String name;
    UUID uuid;
    boolean shortRope = false;
    int hp = 20;
    int hpMax = 20;
    int step = -1; // 0换牌完成 1使用
    int ppNum = 4;
    int ppMax = 4;
    int deckMax = 60;
    int handMax = 9;
    int areaMax = 5;
    List<Card> deck = new ArrayList<>();
    List<Card> hand = new ArrayList<>();
    List<AreaCard> area = new ArrayList<>();
    List<Card> graveyard = new ArrayList<>();
    Integer graveyardCount = 0;// 当墓地消耗时，只消耗计数，不消耗真实卡牌
    public void countToGraveyard(int count){
        graveyardCount += count;
    }
    Map<String,Integer> counter = new ConcurrentHashMap<>();// 计数器
    Leader leader = new Yuwan(this);

    public PlayerInfo(GameInfo info) {
        this.info = info;
    }

    public PlayerInfo getEnemy(){
        return info.anotherPlayerByUuid(getUuid());
    }

    Map<Integer,List<GameRecord>> turnRecords = new HashMap<>();

    public Integer getCount(String key){
        return counter.get(key);
    }
    public void count(String key){
        count(key,1);
    }
    public void count(String key,int time){
        counter.merge(key, time, Integer::sum);
    }

    public void heal(int hp){
        setHp(getHp() + hp);
    }
    public void addHpMax(int hpMax){
        setHpMax(getHpMax() + hpMax);
    }

    public void shuffle(){
        Collections.shuffle(getDeck());
    }
    public void draw(int num){
        info.msg(this.name+"从牌堆中抽了"+num+"张卡牌");
        addHand(deck.subList(0,num));
        deck = deck.subList(num,deck.size());
    }
    public void back(List<Card> cards){
        addDeck(cards);
        hand.removeAll(cards);
    }

    public void addDeck(List<Card> cards){
        int cardsSize = cards.size();
        int deckSize = getDeck().size();
        if(deckSize + cardsSize > deckMax){
            cards.subList(0,deckMax-deckSize);
        }
        info.msg(getName() + "的" + cards.size() + "张卡加入到了牌堆中");
        getDeck().addAll(cards);
        shuffle();
    }
    public void addHand(List<Card> cards){
        String cardNames = cards.stream().map(Card::getName).collect(Collectors.joining("、"));
        info.msgTo(getUuid(),cardNames + "加入了手牌");
        int cardsSize = cards.size();
        int handSize = hand.size();
        int handTotal = handSize + cardsSize;
        if(handTotal > handMax){
            hand.addAll(cards.subList(0,handMax-handSize));
            info.msg(getName()+"的手牌放不下了，多出的"+(handTotal-handMax)+"张牌仅记入墓地数！");

            countToGraveyard(handTotal-handMax);
        }else{
            hand.addAll(cards);
        }
    }
    public void addArea(List<AreaCard> cards){
        int cardsSize = cards.size();
        int areaSize = area.size();
        if(areaSize + cardsSize > areaMax){
            cards.subList(0,areaMax-areaSize);
        }
        area.addAll(cards);
    }

    public void summon(AreaCard areaCard){
        if(getArea().size() == getAreaMax()){
            info.msg(areaCard.getNameWithOwner() + "掉出了战场！");
            return;
        }
        getArea().add(areaCard);
        areaCard.entering();// 发动入场时
    }

    public String describeHand(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getName()).append("\t")
                .append(card.getCost()).append("\t")
                .append(card.getRace()).append("\n")
                .append(card.getMark());
            if(!card.getSubMark().isBlank()){
                sb.append(card.getSubMark()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
