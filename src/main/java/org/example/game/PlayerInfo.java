package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.EquipmentCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.TRANSMIGRATION_NUM;

@Getter
@Setter
public class PlayerInfo {
    GameInfo info;

    String name;
    UUID uuid;
    boolean initative;// 先攻
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
    Leader leader;

    public PlayerInfo(GameInfo info,boolean initative) {
        this.info = info;
        this.initative = initative;
    }

    public PlayerInfo getEnemy(){
        return info.anotherPlayerByUuid(getUuid());
    }

    Map<Integer,List<GameRecord>> turnRecords = new HashMap<>();

    public Integer getCount(String key){
        return Optional.ofNullable(counter.get(key)).orElse(0);
    }
    public void count(String key){
        count(key,1);
    }
    public void clearCount(String key){
        counter.remove(key);
    }
    public void count(String key,int time){
        counter.merge(key, time, Integer::sum);
    }

    public void heal(int hp){
        Damage heal = new Damage(null, this.getLeader(), hp);
        getLeader().useEffectWithDamage(EffectTiming.LeaderHealing,heal);

        if(heal.getDamage()>0){
            int oldHp = getHp();
            setHp(Math.min(getHpMax(),getHp() + heal.getDamage()));
            info.msg(this.getName()+"回复" + (getHp()-oldHp) + "点（剩余"+this.getHp()+"点生命值）");
            getLeader().useEffectWithDamage(EffectTiming.LeaderHealed,heal);
        }else {
            info.msg(this.getName()+"没有回复生命值（剩余"+this.getHp()+"点生命值）");
        }

    }
    public void addHpMax(int hpMax){
        setHpMax(getHpMax() + hpMax);
        info.msg(this.getName()+"血上限提升"+hpMax+"（提升后血量上限为"+this.getHpMax()+"）");
    }

    public void shuffleGraveyard(){
        Collections.shuffle(getGraveyard());
    }
    public void draw(int num){
        info.msg(this.name+"从牌堆中抽了"+num+"张卡牌");
        addHand(deck.subList(0,num));
        deck = deck.subList(num,deck.size());
    }
    public void draw(Predicate<? super Card> condition){
        Optional<Card> findCard = getDeck().stream()
            .filter(condition)
            .findAny();
        if(findCard.isEmpty()){
            info.msg("没有找到可以抽的卡牌！");
            return;
        }
        Card card = findCard.get();
        info.msg(getName()+"搜索并抽到卡牌！");
        addHand(card);
        getDeck().remove(card);
    }
    public void back(Card cards){
        addDeck(cards);
        hand.remove(cards);
    }
    public void back(List<Card> cards){
        addDeck(cards);
        hand.removeAll(cards);
    }

    public void addDeck(Card card){
        addDeck(List.of(card));
    }
    public void addDeck(List<Card> cards){
        int cardsSize = cards.size();
        int deckSize = getDeck().size();
        if(deckSize + cardsSize > deckMax){
            cards.subList(0,deckMax-deckSize);
        }
        info.msg(getName() + "的" + cards.size() + "张卡加入到了牌堆中");
        cards.forEach(card -> {
            int index = (int)(Math.random() * getDeck().size());
            getDeck().add(index,card);
        });
    }
    public void addHand(Card card){
        addHand(List.of(card));
    }
    public void addHand(List<Card> cards){
        String cardNames = cards.stream().map(Card::getName).collect(Collectors.joining("、"));
        info.msgTo(getUuid(),cardNames + "加入了手牌");
        info.msgTo(getEnemy().getUuid(),cards.size() + "张牌加入了对手手牌");
        int cardsSize = cards.size();
        int handSize = hand.size();
        int handTotal = handSize + cardsSize;
        if(handTotal > handMax){
            hand.addAll(cards.subList(0,handMax-handSize));
            info.msg(getName()+"的手牌放不下了，多出的"+(handTotal-handMax)+"张牌从游戏中除外！");

            cards.subList(handMax-handSize,cards.size()).forEach(card -> {
                if(!card.getExiles().isEmpty()){
                    info.msg(card.getNameWithOwner() + "发动除外时效果！");
                    card.getExiles().forEach(exile -> exile.effect().apply());
                }
            });
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
    public List<FollowCard> getAreaFollows(){
        return getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard)
            .map(areaCard -> (FollowCard)areaCard)
            .toList();
    }
    public List<GameObj> getAreaFollowsAsGameObj(){
        return getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard)
            .map(areaCard -> (GameObj)areaCard)
            .toList();
    }

    public void summon(AreaCard areaCard){
        info.msg(getName() + "召唤了" + areaCard.getName());
        if(getArea().size() == getAreaMax()){
            info.msg(areaCard.getNameWithOwner() + "掉出战场，从游戏中除外！");
            if(!areaCard.getExiles().isEmpty()){
                info.msg(areaCard.getNameWithOwner() + "发动除外时效果！");
                areaCard.getExiles().forEach(exile -> exile.effect().apply());
            }
            return;
        }
        getArea().add(areaCard);
        if(!areaCard.getEnterings().isEmpty()){
            info.msg(areaCard.getNameWithOwner() + "发动入场时效果！");
            areaCard.getEnterings().forEach(entering -> entering.effect().apply());// 发动入场时
        }
    }

    public void transmigration(Predicate<? super Card> predicate,int num){
        shuffleGraveyard();
        List<Card> outGraveyardCards = new ArrayList<>();
        getGraveyard().stream().filter(predicate)
            .limit(num).forEach(card -> {
                info.msgTo(getUuid(),card.getName()+"轮回到了牌堆中");
                if(!card.getTransmigrations().isEmpty()){
                    info.msg(card.getNameWithOwner() + "发动轮回时效果！");
                }
                card.getTransmigrations().forEach(entering -> entering.effect().apply());// 发动轮回时
                outGraveyardCards.add(card);
                addDeck(card.copyCard());
            });
        info.msg(getName() + "的"+num+"张牌轮回了");
        count(TRANSMIGRATION_NUM,num);

        getGraveyard().removeAll(outGraveyardCards);
    }

    public String describeHand(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getName()).append("\t")
                .append(card.getCost()).append("\t")
                .append(card.getRace()).append("\n");
            if(card instanceof EquipmentCard equipmentCard){
                sb.append("可用次数：").append(equipmentCard.getCountdown()).append("\n");
            }
            if(!card.getKeywords().isEmpty())
                sb.append(card.getKeywords()).append("\n");
            sb.append(card.getMark());
            if(!card.getSubMark().isBlank())
                sb.append(card.getSubMark()).append("\n");
            sb.append("\n");
        }
        return sb.toString();
    }

}
