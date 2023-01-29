package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.*;
import org.example.card.genshin.LittlePrincess;
import org.example.card.genshin.system.ElementCostSpellCard;
import org.example.constant.EffectTiming;
import org.example.system.Lists;

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
    // 战吼
    boolean canFanfare = true;
    int hp = 20;
    int hpMax = 20;
    int step = -1; // 0换牌完成 1使用
    int ppNum = 0;
    int ppMax = 0;
    int deckMax = 60;
    int handMax = 9;
    int areaMax = 5;
    List<Card> deck = new ArrayList<>();
    List<Card> hand = new ArrayList<>();
    List<AreaCard> area = new ArrayList<>();
    List<Card> graveyard = new ArrayList<>();
    Set<Card> abandon = new HashSet<>();
    Integer graveyardCount = 0;// 当墓地消耗时，只消耗计数，不消耗真实卡牌
    public void countToGraveyard(int count){
        graveyardCount += count;
    }
    Map<String,Integer> counter = new ConcurrentHashMap<>();// 计数器
    Map<Integer,List<Card>> playedCard = new HashMap<>();// 使用卡牌计数器
    Leader leader;

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
        return info.anotherPlayerByUuid(getUuid());
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

    public void shuffleGraveyard(){
        Collections.shuffle(getGraveyard());
    }

    public void steal(int num){
        info.msg(this.name+"从对手牌堆中偷取了"+num+"张卡牌");
        List<Card> enemyDeck = getEnemy().getDeck();
        int finalNum = Math.min(num, enemyDeck.size()) ;// 真正抽到的牌数

        List<Card> cards = enemyDeck.subList(0, finalNum);
        getEnemy().setDeck(enemyDeck.subList(finalNum,enemyDeck.size()));
        addHand(cards);
    }
    public void draw(int num){
        info.msg(this.name+"从牌堆中抽了"+num+"张卡牌");
        int overDraw = num - deck.size();
        int finalNum = num;// 真正抽到的牌数
        if(overDraw>0){
            info.msg(this.name+"从牌堆超抽了"+overDraw+"张卡牌");
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

    public void addDeck(Card card){
        addDeck(List.of(card));
    }
    public void addDeck(List<Card> cards){
        int cardsSize = cards.size();
        int deckSize = getDeck().size();
        if(deckSize + cardsSize > deckMax){
            cards.subList(0,deckMax-deckSize);
        }
        info.msgTo(getEnemy().getUuid(),"对手将" + cards.size() + "张卡洗入牌堆中");
        info.msgTo(getUuid(),
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
        info.msgTo(getUuid(),cardNames.isBlank()?"没有牌":cardNames + "加入了手牌");
        info.msgTo(getEnemy().getUuid(),cards.size() + "张牌加入了对手手牌");
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
            info.msgTo(getUuid(),cardNames + "加入了墓地");
        else
            info.msgTo(getUuid(),cards.size() + "张牌加入了墓地");
        info.msgTo(getEnemy().getUuid(),cards.size() + "张牌加入了对手墓地");
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
        info.msg(getName() + "发动亡灵召还（"+n+"）！");
        Optional<FollowCard> follow = getGraveyard().stream()
            .filter(card -> card instanceof FollowCard && card.getCost()<=n)
            .map(card -> (FollowCard)card)
            .sorted(Comparator.comparingInt(Card::getCost).reversed())
            .findFirst();
        follow.ifPresent(this::recall);
        if (follow.isEmpty()) {
            info.msg(getName() + "发动亡灵召还（"+n+"）失败了，没有召唤任何随从！");
        }

    }
    public void recall(FollowCard followCard){
        info.msg(getName() + "发动亡灵召还！");
        followCard.removeWhenNotAtArea();
        followCard.setHp(followCard.getMaxHp());
        summon(followCard);
    }
    public void abandon(Card card){
        abandon(List.of(card));
    }
    public void abandon(List<Card> cards){
        info.msg(getName() + "舍弃了"+cards.size()+"张卡牌！");
        getHand().removeAll(cards);
        addGraveyard(cards);
        abandon.addAll(cards);
    }

    public void summon(AreaCard summonedCard){
        summon(List.of(summonedCard));
    }

    public void summon(List<AreaCard> summonedCards){
        info.msg(getName() + "召唤了" + summonedCards.stream().map(AreaCard::getId).collect(Collectors.joining("、")));
        addArea(summonedCards);
        info.tempAreaCardEffectBatch(summonedCards,EffectTiming.Entering);

        List<AreaCard> areaCards = getAreaBy(areaCard -> !summonedCards.contains(areaCard));
        info.tempAreaCardEffectBatch(areaCards,EffectTiming.WhenSummon,summonedCards);

        List<AreaCard> enemyAreaCards = getEnemy().getAreaBy(areaCard -> !summonedCards.contains(areaCard));
        info.tempAreaCardEffectBatch(enemyAreaCards,EffectTiming.WhenEnemySummon,summonedCards);
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
        info.msgTo(getUuid(),cards.stream().map(GameObj::getName).collect(Collectors.joining("、"))+"轮回到了牌堆中");
        info.msg(getName() + "的"+cards.size()+"张牌轮回了");
        info.tempCardEffectBatch(cards,EffectTiming.Transmigration);
        count(TRANSMIGRATION_NUM,cards.size());
    }

    public String describePPNum(){
        if(getLeader() instanceof LittlePrincess littlePrincess)
            return "剩余pp：【"+getPpNum()+"】\n"+littlePrincess.showDices();
        else
            return "剩余pp：【"+getPpNum()+"】\n";
    }

    public String describeGraveyard(){
        StringBuilder sb = new StringBuilder();
        sb.append("【墓地】\n");
        for (int i = 0; i < graveyard.size(); i++) {
            Card card = graveyard.get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getId()).append("\t")
                .append(card.getCost()).append("\t")
                .append(card.getRace()).append("\t");
            if(card instanceof EquipmentCard equipmentCard && equipmentCard.getCountdown()>0){
                sb.append("可用次数：").append(equipmentCard.getCountdown());
            }
            sb.append("\n");
        }
        return sb.toString();
    }
    public String describeHand(){
        StringBuilder sb = new StringBuilder();
        sb.append("【手牌信息】\n");
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            sb.append("<p>");
            sb.append("【").append(i+1).append("】\t")
                .append(card.getCost()).append("\t")
                .append(card.getType()).append("\t")
                .append(card.getId()).append("\t")
                .append(String.join("/", card.getRace())).append("\t");
            // region 显示详情
            StringBuilder detail = new StringBuilder();
            if(card instanceof FollowCard followCard)
                detail.append(followCard.getAtk()).append("➹")
                    .append(followCard.getHp()).append("♥");
            if (card instanceof EquipmentCard equipmentCard) {
                sb.append(equipmentCard.getAddAtk()).append("➹")
                    .append(equipmentCard.getAddHp()).append("♥");
                if (equipmentCard.getCountdown() > 0) {
                    sb.append(equipmentCard.getCountdown()).append("⌛︎");
                }
            }
            if (card instanceof AmuletCard amuletCard) {
                if (amuletCard.getCountDown() > 0) {
                    sb.append(amuletCard.getCountDown()).append("⌛︎");
                }
            }
            if (card instanceof ElementCostSpellCard elementCostSpellCard) {
                elementCostSpellCard.getElementCost().forEach(elemental -> {
                    sb.append("【").append(elemental.getStr().replaceAll("元素","")).append("】︎");
                });
            }
            detail.append("<div style='text-align:right;float:right;'>")
                .append(String.join("/",card.getRace())).append("</div>\n");
            if(!card.getKeywords().isEmpty())
                detail.append("<b>")
                    .append(card.getKeywordStr())
                    .append("</b>\n");
            detail.append(card.getMark()).append("\n");
            if(!card.getSubMark().isBlank())
                detail.append("\n").append(card.getSubMark());
            detail.append("\n\n职业：").append(card.getJob());

            sb.append("""
            <icon class="bi bi-eye" style="font-size:18px;"
                    title="%s" data-bs-content="%s" data-placement="auto top"
                    data-container="body" data-bs-toggle="popover"
                      data-trigger="hover" data-html="true"/>
            """.formatted(card.getName(),detail.toString().replaceAll("\\n","<br/>")));
            // endregion
            sb.append("</p>");
        }
        sb.append("<br/>");
        if(getStep()!=-1){// 不是换牌阶段
            if(info.thisPlayer()==this){
                if (getLeader().isCanUseSkill())
                    sb.append("<p>可使用主战者技能：").append(getLeader().getSkillName())
                        .append("（").append(getLeader().getSkillCost()).append("）")
                        .append("""
                   <icon class="bi bi-eye" style="font-size:18px;"
                            title="%s" data-bs-content="%s" data-placement="auto top"
                            data-container="body" data-bs-toggle="popover"
                              data-trigger="hover" data-html="true"/></p>
                    """.formatted(getLeader().getSkillName(),getLeader().getSkillMark().replaceAll("\\n","<br/>")));
                else
                    sb.append("不可使用主战者技能<br/>");
                sb.append(describePPNum());
            }else {
                sb.append("等待对手的回合结束......");
            }
        }
        return sb.toString();
    }


    @Getter
    @Setter
    public static class Weary extends GameObj{
        private String name = "疲劳";
    }
}
