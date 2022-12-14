package org.example.game;

import lombok.Data;
import org.example.card.Card;
import org.example.constant.Patten;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Data
public class GameInfo {
    int turn;
    int turnPlayer;
    boolean gameset;

    PlayerInfo[] playerInfos;

    public GameInfo() {
        this.turn = 0;
        this.turnPlayer = 0;
        this.gameset = false;
        this.playerInfos = new PlayerInfo[2];
        this.playerInfos[0] = new PlayerInfo();
        this.playerInfos[1] = new PlayerInfo();
    }

    public void gameset(){
        // TODO 结束游戏，需要通知到双方玩家
        gameset = true;
    }

    public PlayerInfo thisPlayer(){
        return playerInfos[turnPlayer];
    }
    public PlayerInfo oppositePlayer(){
        return playerInfos[1-turnPlayer];
    }
    public PlayerInfo playerByUuid(UUID uuid){
        if(playerInfos[0].uuid == uuid){
            return playerInfos[0];
        }else {
            return playerInfos[1];
        }
    }
    public PlayerInfo anotherPlayerByUuid(UUID uuid){
        if(playerInfos[0].uuid == uuid){
            return playerInfos[1];
        }else {
            return playerInfos[0];
        }
    }

    public void damageLeader(Leader leader,int damage){
        if(thisPlayer().getLeader().equals(leader)){
            int hp = thisPlayer().hp;
            hp -= damage;
            if(hp < 0){
                gameset();
            }
        }else {
            int hp = oppositePlayer().hp;
            hp -= damage;
            if (hp < 0) {
                gameset();
            }
        }
    }
    @Data
    public static class PlayerInfo{
        String name;
        UUID uuid;
        int hp;
        List<Leader> leaderEffects;
        int[] dices;
        int score;
        AtomicInteger step = new AtomicInteger(-1); // 0换牌完成 1骰子 2使用 3指定
        int diceNum = 0; // 第几次投掷骰子
        int diceMax = 3;
        int deckMax = 60;
        int handMax = 9;
        int areaMax = 5;
        List<Card> deck = new ArrayList<>();
        List<Card> hand = new ArrayList<>();
        List<Card> area = new ArrayList<>();
        List<Card> graveyard = new ArrayList<>();
        Integer graveyardCount = 0;// 当墓地消耗时，只消耗计数，不消耗真实卡牌
        Map<String,Integer> counter = new ConcurrentHashMap<>();// 计数器
        Leader leader = new Leader();

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
                String pattenNames = card.pattens.stream().map(Patten::getName).collect(Collectors.joining("|"));
                sb.append("【").append(i+1).append("】\t")
                    .append(card.getType()).append("\t")
                    .append(card.getName()).append("\t")
                    .append(pattenNames).append("\t")
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

}
