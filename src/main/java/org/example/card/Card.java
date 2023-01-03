package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.function.FunctionN;
import org.example.system.function.PredicateN;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.example.constant.CounterKey.*;
import static org.example.system.Database.getPrototype;

@Getter
@Setter
public abstract class Card extends GameObj {
    public static final int SLOT = 1;
    public static final int APPOSITION = 3;

    private GameObj parent = null;

    private Map<String,Integer> counter = new HashMap<>();

    private List<String> keywords = new ArrayList<>();

    public boolean hasRace(String k){
        return getRace().contains(k);
    }
    public void addKeyword(String k){
        info.msg(getNameWithOwner()+"获得了【"+k+"】");
        getKeywords().add(k);
    }
    public void addKeywords(List<String> ks){
        if(ks.isEmpty())return;
        info.msg(getNameWithOwner()+"获得了【"+ String.join("】【", ks) +"】");
        getKeywords().addAll(ks);
    }
    public boolean hasKeyword(String k){
        return getKeywords().contains(k);
    }
    public int countKeyword(String k){
        return (int) getKeywords().stream().filter(p->p.equals(k)).count();
    }

    public void removeKeywords(List<String> ks){
        ks.forEach(this::removeKeyword);
    }
    public void removeKeyword(String k){
        info.msg(getNameWithOwner()+"失去了【"+ k +"】");
        getKeywords().stream()
            .filter(keyword -> keyword.equals(k))
            .findFirst()
            .ifPresent(s -> getKeywords().remove(s));
    }

    public String getNameWithOwnerWithPlace(){
        String place;
        if(atArea()) place="战场上";
        else if(atHand()) place="手牌中";
        else if(atGraveyard()) place="墓地里";
        else if(atDeck()) place="牌堆中";
        else place="被除外";

        return ownerPlayer().getName()+place+"的"+getName();
    };

    // region 效果列表

    private Play play = null;
    private List<Event.InvocationBegin> invocationBegins = new ArrayList<>();
    private List<Event.InvocationEnd> invocationEnds = new ArrayList<>();
    private List<Event.Transmigration> transmigrations = new ArrayList<>();
    private List<Event.Exile> exiles = new ArrayList<>();
    private List<Event.Boost> boosts = new ArrayList<>();
    private List<Event.Charge> charges = new ArrayList<>();
    private List<Event.WhenKill> whenKills = new ArrayList<>();
    private List<Event.WhenDrawn> whenDrawns = new ArrayList<>();
    // endregion 效果列表


    public abstract String getType();
    public abstract void setCost(Integer cost);
    public abstract Integer getCost();
    public abstract String getJob();
    public abstract List<String> getRace();
    public abstract String getMark();
    public abstract String getSubMark();

    public List<Card> where(){
        if(atArea())return ownerPlayer().getAreaAsCard();
        if(atGraveyard())return ownerPlayer().getGraveyard();
        if(atHand())return ownerPlayer().getHand();
        if(atDeck())return ownerPlayer().getDeck();
        return null;
    }
    public void removeWhenNotAtArea(){
        if(where()==null || atArea())return;
        where().remove(this);
    }
    public void removeWhenAtArea(){
        if(atArea() && this instanceof AreaCard areaCard) {
            ownerPlayer().getArea().remove(this);
            if(!areaCard.getWhenNoLongerAtAreas().isEmpty()) {
                info.msg(getNameWithOwner() + "在场时效果消失！");
                areaCard.getWhenNoLongerAtAreas().forEach(noLongerAtArea -> noLongerAtArea.effect().apply());
            }
        }
    }

    public boolean atArea(){
        return ownerPlayer().getArea().contains(this);
    }
    public boolean atGraveyard(){
        return ownerPlayer().getGraveyard().contains(this);
    }
    public boolean atHand(){
        return ownerPlayer().getHand().contains(this);
    }
    public boolean atDeck(){
        return ownerPlayer().getDeck().contains(this);
    }



    public void count(){
        count(DEFAULT,1);
    }
    public void count(int time){
        count(DEFAULT,time);
    }
    public Integer getCount(){
        return Optional.ofNullable(counter.get(DEFAULT)).orElse(0);
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

    public Card copyCard(){
        try {
            Card card = this.getClass().getDeclaredConstructor().newInstance();
            card.parent = this;
            card.owner = this.owner;
            card.info = this.info;
            card.initCounter();
            return card;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Card prototype(){
        try {
            return getPrototype(getClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void addToName(String add){
        setName(getName() + add);
    }
    public boolean isRealName(){
        return getName().equals(prototype().getName());
    }
    public void exposeRealName(){
        if(!isRealName()){
            info.msg(getNameWithOwner() + "改名为" + prototype().getName());
            setName(prototype().getName());
        }
    }


    public void play(List<GameObj> targets,int choice){
        if(ownerPlayer().getPpNum() < getCost()){
            info.msgToThisPlayer("你没有足够的PP来使用该卡牌！");
            throw new RuntimeException();
        }
        info.msg(ownerPlayer().getName() + "使用了" + getName());

        // region 消耗PP
        int ppNum = ownerPlayer().getPpNum() - getCost();
        ownerPlayer().setPpNum(ppNum);
        ownerPlayer().count(ALL_COST,getCost());
        // endregion 消耗PP

        // region 驻场卡召唤到场上(装备卡装备给随从)，法术卡丢到墓地
        if(this instanceof AreaCard areaCard){
            if(this instanceof EquipmentCard equipmentCard){
                if(targets.size()!=1 || !(targets.get(0) instanceof FollowCard target)){
                    info.msgToThisPlayer("无法使用装备卡！");
                    throw new RuntimeException();
                }
                target.equip(equipmentCard);
            }else {
                ownerPlayer().summon(areaCard);
            }
        } else if (this instanceof SpellCard) {
            ownerPlayer().getGraveyard().add(this);
            ownerPlayer().countToGraveyard(1);
        }
        ownerPlayer().getHand().remove(this);
        // endregion

        // region 发动卡牌效果
        if(getPlay() != null){
            if (this instanceof AreaCard) {
                info.msg(getNameWithOwner() + "发动战吼");
            }
            getPlay().effect().accept(choice,targets);
        }
        // endregion 发动卡牌效果

        // 触发手牌上全部增幅效果
        ownerPlayer().getHandCopy().stream().map(Card::getBoosts)
            .flatMap(Collection::stream)
            .filter(boost -> boost.canBeTriggered.test(this))
            .forEach(boost->boost.effect.accept(this));

        ownerPlayer().count(PLAY_NUM);

        info.msgTo(ownerPlayer().getUuid(),
            info.describeArea(ownerPlayer().getUuid()) + ownerPlayer().describePPNum());
        info.msgTo(enemyPlayer().getUuid(),
            info.describeArea(enemyPlayer().getUuid()) + ownerPlayer().describePPNum());
    }

    public static class Event {
        /** 回合开始瞬召 */
        public record InvocationBegin(PredicateN canBeTriggered, FunctionN effect){}
        /** 回合结束瞬召 */
        public record InvocationEnd(PredicateN canBeTriggered, FunctionN effect){}
        /** 轮回(由墓地进入牌堆) */
        public record Transmigration(FunctionN effect){}
        /** 除外(从游戏中除外) */
        public record Exile(FunctionN effect){}
        /** 增幅(其他卡牌被使用) */
        public record Boost(Predicate<Card> canBeTriggered, Consumer<Card> effect){
            public Boost(Predicate<Card> canBeTriggered,FunctionN effect){
                this(canBeTriggered,card -> effect.apply());
            }
        }
        /** 注能(场上卡牌被破坏) */
        public record Charge(Predicate<Card> canBeTriggered, Consumer<Card> effect){}
        /** 击杀时 */
        public record WhenKill(Consumer<FollowCard> effect){}
        /** 被抽到时 */
        public record WhenDrawn(FunctionN effect){}
    }
}
