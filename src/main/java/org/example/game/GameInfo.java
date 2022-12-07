package org.example.game;

import lombok.Data;
import org.example.card.Card;

import java.util.*;

@Data
public class GameInfo {
    int turn;
    int turnPlayer = 1;
    boolean gameset = false;


    PlayerInfo[] playerInfos = new PlayerInfo[2];

    public void gameset(){
        // TODO 结束游戏，需要通知到双方玩家
        gameset = true;
    }

    public PlayerInfo thisPlayer(){
        return playerInfos[turnPlayer];
    }
    public PlayerInfo oppositePlayer(){
        return playerInfos[3-turnPlayer];
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
        int step = 0; // 0换牌 1骰子 2使用 3指定
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
        Map<String,Integer> counter = new HashMap<>();// 计数器
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

    }

}
