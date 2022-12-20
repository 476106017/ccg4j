package org.example.game;

import lombok.Data;
import org.example.card.Card;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class PlayerInfo {

    String name;
    UUID uuid;
    int hp = 20;
    int hpMax = 20;
    List<Leader> leaderEffects;
    int step = -1; // 0换牌完成 1使用 2指定
    int ppNum = 0;
    int ppMax = 0;
    int deckMax = 60;
    int handMax = 9;
    int areaMax = 5;
    List<Card> deck = new ArrayList<>();
    List<Card> hand = new ArrayList<>();
    List<Card> area = new ArrayList<>();
    List<Card> graveyard = new ArrayList<>();
    Integer graveyardCount = 0;// 当墓地消耗时，只消耗计数，不消耗真实卡牌
    public void countToGraveyard(int count){
        graveyardCount += count;
    }
    Map<String,Integer> counter = new ConcurrentHashMap<>();// 计数器
    Leader leader = new Leader();

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


    public void shuffle(){
        Collections.shuffle(deck);
    }
    public void draw(int num){
        addHand(deck.subList(0,num));
        deck = deck.subList(num,deck.size());
    }
    public void back(List<Card> cards){
        addDeck(cards);
        hand.removeAll(cards);
    }

    public void addDeck(List<Card> cards){
        int cardsSize = cards.size();
        int deckSize = deck.size();
        if(deckSize + cardsSize > deckMax){
            cards.subList(0,deckMax-deckSize);
        }
        deck.addAll(cards);
    }
    public void addHand(List<Card> cards){
        int cardsSize = cards.size();
        int handSize = hand.size();
        if(handSize + cardsSize > handMax){
            hand.addAll(cards.subList(0,handMax-handSize));
            addGraveyard(cards.subList(handMax-handSize,cardsSize));// 手牌爆的牌全到墓地
        }else{
            hand.addAll(cards);
        }
    }
    public void addArea(List<Card> cards){
        int cardsSize = cards.size();
        int areaSize = area.size();
        if(areaSize + cardsSize > areaMax){
            cards.subList(0,areaMax-areaSize);
        }
        area.addAll(cards);
    }
    public void addGraveyard(List<Card> cards){
        graveyard.addAll(cards);
        graveyardCount+=cards.size();
    }

    public String describeHand(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getName()).append("\t")
                .append(card.getCost()).append("\t")
                .append(card.getJob()).append("\n")
                .append(card.getMark());
            if(!card.getSubMark().isBlank()){
                sb.append(card.getSubMark()).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
