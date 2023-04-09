package org.example.game;

import jakarta.websocket.EncodeException;
import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.CounterKey;
import org.example.constant.DeckPreset;
import org.example.constant.EffectTiming;
import org.example.system.Database;
import org.example.system.util.FunctionN;
import org.example.system.util.Lists;
import org.example.system.util.Msg;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.TRANSMIGRATION_NUM;

@Getter
@Setter
public class PlayerInfo implements Serializable {
    transient GameInfo info;

    String name;
    transient Session session;
    boolean initative;// 先攻
    boolean shortRope = false;
    // 战吼
    boolean canFanfare = true;
    int hp = 30;
    int hpMax = 30;
    int step = -1; // 0换牌完成 1发现
    int discoverMax = 3; // 发现卡牌数量
    transient Thread discoverThread = null;
    int discoverNum = 0; // 发现卡牌序号
    int ppNum = 0;
    int ppMax = 0;
    int ppLimit = 10;
    int deckMax = 60;
    int handMax = 9;
    int areaMax = 7;
    transient List<Card> deck = new ArrayList<>();
    List<Card> hand = new ArrayList<>();
    Predicate<Card> handPlayable = card -> true;
    List<AreaCard> area = new ArrayList<>();
    transient List<Card> graveyard = new ArrayList<>();
    transient Set<Card> abandon = new HashSet<>();
    Integer graveyardCount = 0;// 当墓地消耗时，只消耗计数，不消耗真实卡牌

    Integer deckCount; // 前端展示用


    public void countToGraveyard(int count){
        graveyardCount = Math.max(0, graveyardCount + count);
    }
    public boolean costGraveyardCountTo(int cost, FunctionN function){
        if(graveyardCount < cost){
            info.msg(this.getName()+"没有足够的墓地来发动死灵术！");
            return false;
        }
        countToGraveyard(-cost);
        count(CounterKey.NECROMANCY_NUM,cost);
        info.tempEffectBatch(getAreaFollowsAsGameObj(),EffectTiming.WhenCostGraveyard, cost);
        info.startEffect();
        function.apply();
        return true;
    }
    public boolean costMoreGraveyardCountTo(int costMax, Consumer<Integer> consumer){
        if(graveyardCount == 0){
            info.msg(this.getName()+"没有足够的墓地来发动死灵术！");
            return false;
        }
        int cost = Math.min(graveyardCount,costMax);
        countToGraveyard(-cost);
        count(CounterKey.NECROMANCY_NUM,cost);
        info.tempEffectBatch(getAreaFollowsAsGameObj(),EffectTiming.WhenCostGraveyard, cost);
        info.startEffect();
        consumer.accept(cost);
        return true;
    }

    Map<String,Integer> counter = new ConcurrentHashMap<>();// 计数器
    Map<Integer,List<Card>> playedCard = new HashMap<>();// 使用卡牌计数器
    Leader leader;

    public void setLeader(Leader leader) {
        this.leader = leader;
        info.msg(getName()+"的主战者变成了"+leader.getName());
    }

    // 自动发现并继续同步执行
    public void autoDiscover(){
        while(getStep() > 0){// 连续发现的，全部自动完成
            setDiscoverNum(0);
            getDiscoverThread().run();// 就用run，不用有并发线程
        }
    }
    // 发现1张牌并执行consumer
    public void discoverCard(Predicate<Card> predicate,Consumer<Card> consumer){
        List<Card> prototypes = new ArrayList<>(Database.getPrototypeBy(predicate, getDiscoverMax()));
        // 发现的是原型卡，所以回调时要先创造一张它的克隆
        discoverCard(prototypes, consumer);
    }

    // 发现并获得己方现有卡
    public void discoverCard(List<Card> cards,Consumer<Card> consumer){
        if(cards.isEmpty()) return;
        if(cards.size()==1){
            consumer.accept(cards.get(0));
            return;
        }

        // 让玩家选择
        step++;
        StringBuilder sb = new StringBuilder("发现卡牌：");
        AtomicInteger num = new AtomicInteger(1);

        ArrayList<Card> cardsCopy = new ArrayList<>(cards);
        Collections.shuffle(cardsCopy);

        Msg.send(getSession(),"discover", cardsCopy);

        discoverThread = new Thread(()->{
            Card discoverCard;
            if(discoverNum==0 || cardsCopy.size() < discoverNum)
                discoverCard= Lists.randOf(cardsCopy);
            else
                discoverCard= cardsCopy.get(discoverNum-1);

            consumer.accept(discoverCard);
            step--;
            discoverNum = 0;
        });

    }

    private int weary = 1;// 疲劳

    public int countWeary(){
        return weary++;
    }

    public void wearyDamaged(){
        getLeader().damaged(new Weary(),countWeary());
    }

    public PlayerInfo(GameInfo info,boolean initative) {
        this.info = info;
        this.initative = initative;
    }

    public PlayerInfo getEnemy(){
        return info.anotherPlayerBySession(getSession());
    }

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
        getLeader().useEffects(EffectTiming.LeaderHealing,heal);

        if(heal.getDamage()>0){
            int oldHp = getHp();
            setHp(Math.min(getHpMax(),getHp() + heal.getDamage()));
            info.msg(this.getName()+"回复" + (getHp()-oldHp) + "点（剩余"+this.getHp()+"点生命值）");
            getLeader().tempEffects(EffectTiming.LeaderHealed,heal);
        }else {
            info.msg(this.getName()+"没有回复生命值（剩余"+this.getHp()+"点生命值）");
        }

    }
    public void addHpMax(int hpMax){
        setHpMax(getHpMax() + hpMax);
        info.msg(this.getName()+"血上限提升"+hpMax+"（提升后血量上限为"+this.getHpMax()+"）");
    }
    public void addPp(int num){
        if(num>0){
            int pp = Math.min(getPpLimit(),getPpNum()+num);
            setPpNum(pp);
        } else if (num<0) {
            int pp = Math.max(0,getPpNum()+num);
            setPpNum(pp);
        }
    }

    public void shuffleGraveyard(){
        Collections.shuffle(getGraveyard());
    }

    public boolean burial(FollowCard followCard){
        if(getArea().size()>=getAreaMax()){
            info.msg(this.name+"的场上没有足够的空间，葬送失败");
            return false;
        }
        info.msg(this.name+"正在葬送："+followCard.getName());
        followCard.purify();
        followCard.removeWhenNotAtArea();
        summon(followCard);
        followCard.death();
        return true;
    }

    public void steal(int num){
        info.msg(this.name+"从对手牌堆中偷取了"+num+"张卡牌");
        List<Card> enemyDeck = getEnemy().getDeck();
        int finalNum = Math.min(num, enemyDeck.size()) ;// 真正抽到的牌数

        List<Card> cards = enemyDeck.subList(0, finalNum);
        getEnemy().setDeck(enemyDeck.subList(finalNum,enemyDeck.size()));
        addHand(cards);
    }
    public List<Card> draw(int num){
        info.msg(this.name+"从牌堆中抽了"+num+"张卡牌");
        Msg.send(getSession(),"draw",num);
        Msg.send(getEnemy().getSession(), "enemyDraw",num);
        int overDraw = num - deck.size();
        int finalNum = num;// 真正抽到的牌数
        if(overDraw>0){
            info.msg(this.name+"从牌堆超抽了"+overDraw+"张卡牌");
            Msg.send(getSession(),"overdraw",overDraw);
            Msg.send(getEnemy().getSession(), "enemyOverdraw",overDraw);
            getLeader().getOverDraw().accept(overDraw);
            getLeader().tempEffects(EffectTiming.WhenOverDraw,overDraw);
            getEnemy().getLeader().tempEffects(EffectTiming.WhenEnemyOverDraw,overDraw);
            finalNum = deck.size();
        }
        List<Card> cards = deck.subList(0, finalNum);
        deck = deck.subList(finalNum,deck.size());
        addHand(cards);

        if(getStep()>-1){
            info.tempEffectBatch(getAreaAsGameObj(),EffectTiming.WhenDraw,cards);
            info.tempEffectBatch(getEnemy().getAreaAsGameObj(),EffectTiming.WhenEnemyDraw,cards);
        }

        return cards;
    }
    public Card draw(Predicate<? super Card> condition){
        List<Card> draws = draw(condition, 1);
        if(draws.size()>0){
            return draws.get(0);
        }
        return null;
    }
    public List<Card> draw(Predicate<? super Card> condition, int num){
        List<Card> searches = getDeck().stream().filter(condition).limit(num)
            .toList();
        if(searches.isEmpty()){
            info.msg(getName()+"搜索失败！");
            return searches;
        }
        info.msg(getName()+"搜索到了"+num+"张牌！");
        addHand(searches);
        getDeck().removeAll(searches);
        return searches;
    }
    public void backToDeck(Card cards){
        addDeck(cards);
        hand.remove(cards);
    }
    public void backToDeck(List<Card> cards){
        addDeck(cards);
        hand.removeAll(cards);
    }

    public void addDeckBottom(Card card){
        if(getDeck().size()==deckMax)return;
        info.msgTo(getEnemy().getSession(),"对手将1张卡放到牌堆底部");
        info.msgTo(getSession(),card.getName() + "被放到牌堆底部");
        getDeck().add(card);
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
        info.msgTo(getEnemy().getSession(),"对手将" + cards.size() + "张卡洗入牌堆中");
        info.msgTo(getSession(),
            cards.stream().map(GameObj::getName).collect(Collectors.joining("、")) + "被洗入牌堆");
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
        info.msgTo(getSession(),cardNames.isBlank()?"没有牌":cardNames + "加入了手牌");
        info.msgTo(getEnemy().getSession(),cards.size() + "张牌加入了对手手牌");
        int cardsSize = cards.size();
        int handSize = hand.size();
        int handTotal = handSize + cardsSize;
        List<Card> successCards;
        if(handTotal > handMax){
            successCards = cards.subList(0,handMax-handSize);
            info.msg(getName()+"的手牌放不下了，多出的"+(handTotal-handMax)+"张牌从游戏中除外！");

            List<Card> exileCards = cards.subList(handMax - handSize, cards.size());
            info.tempCardEffectBatch(exileCards,EffectTiming.Exile);

        }else{
            successCards = cards;
        }
        hand.addAll(successCards);

        if(getStep()>-1)
            successCards.forEach(card -> card.tempEffects(EffectTiming.WhenDrawn));

    }
    public void addArea(AreaCard areaCard){
        addArea(List.of(areaCard));
    }
    public void addArea(List<AreaCard> cards){
        int i = cards.size() + area.size() - areaMax;
        List<AreaCard> exileCards = new ArrayList<>();
        if(i>0){
            exileCards = cards.subList(cards.size() - i, cards.size());
            info.msg(getName()+"的战场放不下了，多出的"+i+"张牌从游戏中除外！");
            info.tempAreaCardEffectBatch(exileCards,EffectTiming.Exile);
        }
        getArea().addAll(cards);
        getArea().removeAll(exileCards);
        info.tempAreaCardEffectBatch(cards,EffectTiming.WhenAtArea);
    }
    public void addGraveyard(Card card){
        addGraveyard(List.of(card));
    }
    public void addGraveyard(List<Card> cards){
        String cardNames = cards.stream().map(Card::getName).collect(Collectors.joining("、"));
        if(cards.size()<10)
            info.msgTo(getSession(),cardNames + "加入了墓地");
        else
            info.msgTo(getSession(),cards.size() + "张牌加入了墓地");
        info.msgTo(getEnemy().getSession(),cards.size() + "张牌加入了对手墓地");
        graveyardCount+=cards.size();
        graveyard.addAll(cards);
    }
    // 卡牌的快照。用来循环（原本卡牌List可以随便删）
    public List<AreaCard> getAreaCopy(){
        return new ArrayList<>(getArea());
    }
    public List<Card> getHandCopy(){
        return new ArrayList<>(getHand());
    }
    public List<Card> getDeckCopy(){
        return new ArrayList<>(getDeck());
    }
    public List<Card> getGraveyardCopy(){
        return new ArrayList<>(getGraveyard());
    }
    public List<Card> getGraveyardBy(Predicate<Card> p){
        return getGraveyard().stream().filter(p).toList();
    }

    public List<FollowCard> getHandFollows(){
        return getHand().stream().filter(card -> card instanceof FollowCard)
            .map(card -> (FollowCard) card).toList();
    }
    public List<Card> getHandBy(Predicate<Card> p){
        return getHand().stream().filter(p).toList();
    }
    public List<Card> getDeckBy(Predicate<Card> p){
        return getDeck().stream().filter(p).toList();
    }
    public List<GameObj> getHandAsGameObjBy(Predicate<Card> p){
        return getHand().stream().filter(p).map(i->(GameObj)i).toList();
    }
    public List<AreaCard> getAreaBy(Predicate<AreaCard> p){
        return getArea().stream().filter(p).toList();
    }
    public List<Card> getAreaAsCard(){
        return getArea().stream()
            .map(areaCard -> (Card)areaCard)
            .toList();
    }
    public List<GameObj> getAreaAsGameObj(){
        return getArea().stream()
            .map(areaCard -> (GameObj)areaCard)
            .toList();
    }
    public List<GameObj> getAreaAsGameObjBy(Predicate<AreaCard> p){
        return getArea().stream()
            .filter(p)
            .map(areaCard -> (GameObj)areaCard)
            .toList();
    }
    public List<FollowCard> getAreaFollowsAsFollow(){
        return getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard)
            .map(areaCard -> (FollowCard)areaCard)
            .toList();
    }
    public FollowCard getDeckRandomFollow(){
        List<FollowCard> areaCards = getDeck().stream()
            .filter(card -> card instanceof FollowCard)
            .map(card -> (FollowCard)card)
            .toList();
        return Lists.randOf(areaCards);
    }
    public AreaCard getAreaRandomFollow(){
        List<AreaCard> areaCards = getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard)
            .toList();
        return Lists.randOf(areaCards);
    }
    public FollowCard getHandRandomFollow(){
        List<FollowCard> areaCards = getHand().stream()
            .filter(card -> card instanceof FollowCard)
            .map(card -> (FollowCard)card)
            .toList();
        return Lists.randOf(areaCards);
    }

    public AreaCard getAreaRandomGuardFollow(){
        List<AreaCard> areaCards = getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard followCard
                && followCard.hasKeyword("守护"))
            .toList();
        return Lists.randOf(areaCards);
    }

    public List<AreaCard> getAreaFollows(){
        return getAreaFollows(true);
    }
    public List<AreaCard> getAreaFollows(boolean ignoreGuard){
        if(ignoreGuard)
            return getArea().stream()
                .filter(areaCard -> areaCard instanceof FollowCard)
                .toList();
        else{
            List<AreaCard> guards = getArea().stream()
                .filter(areaCard -> areaCard instanceof FollowCard followCard && followCard.hasKeyword("守护"))
                .toList();
            if(guards.isEmpty())// 没有守护随从，则返回全部随从
                return getAreaFollows(true);
            return guards;// 有守护随从则返回
        }
    }
    public List<AreaCard> getAreaFollowsBy(Predicate<FollowCard> p){
        return getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard followCard && p.test(followCard))
            .toList();
    }
    public List<FollowCard> getAreaCanAttackFollows(){
        List<FollowCard> shogoFollows = getAreaFollowsAsFollowBy(followCard -> followCard.hasKeyword("守护"));
        if(!shogoFollows.isEmpty())
            return shogoFollows;

        return getAreaFollowsAsFollow();
    }
    public List<FollowCard> getAreaFollowsAsFollowBy(Predicate<FollowCard> p){
        return getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard followCard && p.test(followCard))
            .map(areaCard -> (FollowCard)areaCard)
            .toList();
    }
    public List<Card> getAreaFollowsAsCardBy(Predicate<FollowCard> p){
        return getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard followCard && p.test(followCard))
            .map(areaCard -> (Card)areaCard)
            .toList();
    }
    public List<GameObj> getAreaFollowsAsGameObj(){
        return getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard)
            .map(areaCard -> (GameObj)areaCard)
            .toList();
    }
    public List<GameObj> getAreaFollowsAsGameObjBy(Predicate<FollowCard> p){
        return getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard followCard && p.test(followCard))
            .map(areaCard -> (GameObj)areaCard)
            .toList();
    }

    public void recall(int n){
        recall(n,follow->{});
    }
    public void recall(int n, Consumer<FollowCard> callback){
        info.msg(getName() + "发动亡灵召还（"+n+"）！");
        Optional<FollowCard> follow = getGraveyard().stream()
            .filter(card -> card instanceof FollowCard && card.getCost()<=n)
            .map(card -> (FollowCard)card)
            .sorted(Comparator.comparingInt(Card::getCost).reversed())
            .findFirst();
        follow.ifPresent(this::recall);
        if (follow.isEmpty()) {
            info.msg(getName() + "发动亡灵召还（"+n+"）失败了，没有召唤任何随从！");
        }else {
            callback.accept(follow.get());
        }
    }
    public void recall(List<AreaCard> recalledCards){
        info.msg(getName() + "发动亡灵召还！");
        recalledCards.forEach(areaCard -> {
            areaCard.removeWhenNotAtArea();
            if(areaCard instanceof FollowCard followCard)
                followCard.setHp(followCard.getMaxHp());
        });
        List<AreaCard> areaCopy = getAreaCopy();// 召还前的场上卡牌
        summon(recalledCards);
        info.tempAreaCardEffectBatch(recalledCards,EffectTiming.WhenRecalled);
        info.tempAreaCardEffectBatch(areaCopy,EffectTiming.WhenOthersRecall,recalledCards);
    }
    public void recall(AreaCard areaCard){
        recall(List.of(areaCard));
    }

    public void abandon(int num){
        abandon(Lists.randOf(getHandCopy(),num));
    }
    public void abandon(Card card){
        abandon(List.of(card));
    }
    public void abandon(List<Card> cards){
        info.msg(getName() + "舍弃了"+cards.size()+"张卡牌！");
        getHand().removeAll(cards);
        addGraveyard(cards);
        abandon.addAll(cards);
        info.tempCardEffectBatch(cards,EffectTiming.WhenAbandoned);
        info.tempAreaCardEffectBatch(getAreaCopy(),EffectTiming.WhenAbandon,cards);
    }

    public void hire(Predicate<AreaCard> predicate){
        hire(predicate,1);
    }
    public void hire(Predicate<AreaCard> predicate, int n){

        List<AreaCard> cards = getDeckBy(card ->
            card instanceof AreaCard areaCard && predicate.test(areaCard))
            .stream().map(p->(AreaCard)p).toList();
        if(!cards.isEmpty()){
            List<AreaCard> prepareToHire = cards.subList(0, Math.min(cards.size(), n));

            prepareToHire.forEach(Card::removeWhenNotAtArea);
            summon(prepareToHire);
        }else {
            info.msg("招募失败！");
        }
    }
    public void hire(Predicate<AreaCard> predicate, Consumer<AreaCard> consumer){
        List<AreaCard> cards = getDeckBy(card ->
            card instanceof AreaCard areaCard && predicate.test(areaCard))
            .stream().map(p->(AreaCard)p).toList();
        if(!cards.isEmpty()){
            AreaCard hireCard = cards.get(0);

            hireCard.removeWhenNotAtArea();
            summon(hireCard);
            consumer.accept(hireCard);
        }else {
            info.msg("招募失败！");
        }
    }

    public void summon(AreaCard summonedCard){
        summon(List.of(summonedCard));
    }

    public void summon(List<AreaCard> summonedCards){
        info.msg(getName() + "召唤了" + summonedCards.stream().map(AreaCard::getId).collect(Collectors.joining("、")));
        addArea(summonedCards);
        info.useAreaCardEffectBatch(summonedCards,EffectTiming.Entering);

        List<AreaCard> areaCards = getAreaBy(areaCard -> !summonedCards.contains(areaCard));
        info.useAreaCardEffectBatch(areaCards,EffectTiming.WhenSummon,summonedCards);
        getLeader().useEffects(EffectTiming.WhenSummon,summonedCards);

        List<AreaCard> enemyAreaCards = getEnemy().getAreaBy(areaCard -> !summonedCards.contains(areaCard));
        info.useAreaCardEffectBatch(enemyAreaCards,EffectTiming.WhenEnemySummon,summonedCards);
        getLeader().useEffects(EffectTiming.WhenEnemySummon,summonedCards);
    }

    public void transmigration(Predicate<? super Card> predicate,int num){
        shuffleGraveyard();
        List<Card> cards = getGraveyardCopy().stream().filter(predicate).limit(num).toList();
        if(cards.isEmpty()){
            info.msg("没有符合要求的牌，轮回失败！");
            return;
        }
        getGraveyard().removeAll(cards);
        addDeck(cards);
        info.msgTo(getSession(),cards.stream().map(GameObj::getName).collect(Collectors.joining("、"))+"轮回到了牌堆中");
        info.msg(getName() + "的"+cards.size()+"张牌轮回了");
        info.tempCardEffectBatch(cards,EffectTiming.Transmigration);
        count(TRANSMIGRATION_NUM,cards.size());
    }

    @Getter
    @Setter
    public static class Weary extends GameObj{
        private String name = "疲劳";
    }
}
