package org.example.card;

import lombok.Getter;
import lombok.Setter;
import org.example.constant.EffectTiming;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.game.PlayerInfo;
import org.example.system.util.Msg;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.*;
import static org.example.system.Database.getPrototype;

@Getter
@Setter
public abstract class Card extends GameObj implements Serializable {
    public static final int SLOT = 1;
    public static final int APPOSITION = 3;

    private transient GameObj parent = null;

    public void changeParent(GameObj obj){
        info.msg(getNameWithOwner()+"的创造者被变更为："+obj.getName());
        this.parent = obj;
    }

    private Map<String,Integer> counter = new HashMap<>();

    private List<String> keywords = new ArrayList<>();


    private boolean upgrade = false;

    private String subMarkStr = "";

    public void upgrade(){
        if(isUpgrade())return;
        info.msg(getNameWithOwner() + "进化了！");
        setUpgrade(true);
    }

    public boolean hasRace(String k){
        return getRace().contains(k);
    }
    public String getKeywordStr(){
        Map<String, Long> count = keywords.stream()
            .collect(Collectors.groupingBy(k->k, Collectors.counting()));
        List<String> keywordWithCount = new ArrayList<>();
        count.forEach(((k,v)->{
            if(v.intValue()==1)
                keywordWithCount.add(k);
            else
                keywordWithCount.add(k+"("+v+")");
        }));
        return String.join(" ", keywordWithCount);
    }
    public void addKeyword(String k){
        info.msg(getNameWithOwner()+"获得了【"+k+"】");
        getKeywords().add(k);
    }
    public void addKeywordN(String k,int n){
        info.msg(getNameWithOwner()+"获得了"+n+"层【"+k+"】");
        for (int i = 0; i < n; i++) {
            getKeywords().add(k);
        }
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
        getKeywords().stream()
            .filter(keyword -> keyword.equals(k))
            .findFirst()
            .ifPresent(s -> {
                getKeywords().remove(s);
                if(hasKeyword(s))
                    info.msg(getNameWithOwner()+"失去了1层【"+ k +"】");
                else
                    info.msg(getNameWithOwner()+"失去了【"+ k +"】");
            });
    }
    public void removeKeyword(String k,int n){
        List<String> keys = getKeywords().stream()
            .filter(keyword -> keyword.equals(k)).toList();
        if(keys.size()>0){
            int min = Math.min(n, keys.size());
            for (int i = 0; i < min; i++) {
                getKeywords().remove(keys.get(i));
            }
            info.msg(getNameWithOwner()+"失去了"+min+"层【"+ k +"】");
        }
    }
    public void removeKeywordAll(String k){
        List<String> keys = getKeywords().stream()
            .filter(keyword -> keyword.equals(k)).toList();
        if(keys.size()>0){
            info.msg(getNameWithOwner()+"失去了"+keys.size()+"层【"+ k +"】");
            getKeywords().removeAll(keys);
        }
    }

    public String getNameWithOwnerWithPlace(){
        String place;
        if(atArea()) place="战场上";
        else if(atHand()) place="手牌中";
        else if(atGraveyard()) place="墓地里";
        else if(atDeck()) place="牌堆中";
        else place="被除外";

        return ownerPlayer().getName()+place+"的"+getName();
    }

    private Play play = null;

    public abstract String getType();
    public abstract void setCost(Integer cost);
    public abstract Integer getCost();

    public void addCost(Integer cost){
        setCost(Math.max(0,getCost() + cost));
        info.msg(getNameWithOwner()+"的费用变成了"+getCost());
    }
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
        if(atGraveyard() && this instanceof FollowCard followCard)
            followCard.setHp(followCard.getMaxHp());// 移出墓地时回满生命值
        where().remove(this);
    }
    public void removeWhenAtArea(){
        if(atArea() && this instanceof AreaCard areaCard) {

            List<AreaCard> area = ownerPlayer().getArea();
            areaCard.setLeaveIndex(area.indexOf(this));
            area.remove(this);
            areaCard.useEffects(EffectTiming.WhenNoLongerAtArea);

            List<Card> skills = ownerPlayer().getHandCopy().stream()
                .filter(card -> card.getRace().contains("灵魂绑定") && card.getParent() == this)
                .toList();

            if(!skills.isEmpty()){
                getInfo().exile(skills);
                getInfo().msg(getNameWithOwner() + "的灵魂绑定从手牌中除外了");
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
    public void clearCount(){
        counter.remove(DEFAULT);
    }
    public void clearCount(String key){
        counter.remove(key);
    }
    public void count(String key,int time){
        counter.merge(key, time, Integer::sum);
    }

    public Card copy(){
        return copyBy(ownerPlayer());
    }

    public Card copyBy(PlayerInfo player){
        Card card = createCard(this.getClass());

        card.setParent(player.getLeader());
        card.setInfo(player.getInfo());
        card.setOwner(player.getInfo().getPlayerInfos()[0]==player?0:1);

        card.setKeywords(new ArrayList<>(getKeywords()));
        if(this instanceof FollowCard followCard && card instanceof FollowCard followCardCopy){
            followCardCopy.setAtk(followCard.getAtk());
            followCardCopy.setMaxHp(followCard.getMaxHp());
            followCardCopy.setHp(followCard.getHp());
        }
        Object counter = ((HashMap) getCounter()).clone();
        card.setCounter((Map)counter);
        return card;
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
        if(!ownerPlayer().getHandPlayable().test(this)){
            Msg.warn(ownerPlayer().getSession(), "由于限制，目前无法使用这张卡牌！");
            return;
        }
        if(ownerPlayer().getPpNum() < getCost()){
            Msg.warn(ownerPlayer().getSession(), "你没有足够的PP来使用该卡牌！");
            return;
        }
        info.msg(ownerPlayer().getName() + "使用了" + getName());

        // region 消耗PP
        int ppNum = ownerPlayer().getPpNum() - getCost();
        ownerPlayer().setPpNum(ppNum);
        ownerPlayer().count(ALL_COST,getCost());
        // endregion 消耗PP

        // region 在使用卡牌造成任何影响前，先计算使用时
        ownerLeader().useEffects(EffectTiming.WhenPlay,this);
        enemyLeader().useEffects(EffectTiming.WhenEnemyPlay,this);
        ownerPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.WhenPlay,this));
        enemyPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.WhenEnemyPlay,this));
        // endregion 在使用卡牌造成任何影响前，先计算使用时

        ownerPlayer().getHand().remove(this);
        // region 驻场卡召唤到场上(装备卡装备给随从)，法术卡丢到墓地
        if(this instanceof AreaCard areaCard){
            if(this instanceof EquipmentCard equipmentCard){
                if(targets.size()!=1 || !(targets.get(0) instanceof FollowCard target)){
                    Msg.warn(ownerPlayer().getSession(), "无法使用装备卡！");
                    return;
                }
                target.equip(equipmentCard);
            }else {
                ownerPlayer().summon(areaCard);
            }
        } else if (this instanceof SpellCard spellCard) {
            if(spellCard.hasKeyword("灵魂绑定")){
                Msg.send(ownerPlayer().getSession(), "灵魂绑定！");
                ownerPlayer().addHand(this);
            }else{
                ownerPlayer().getGraveyard().add(this);
                ownerPlayer().countToGraveyard(1);
            }
        }
        // endregion

        // region 发动卡牌效果
        if(getPlay() != null){
            // 没有可选择目标时不发动效果
            if(getPlay().targetNum()>0 && targets.isEmpty()){
                info.msg(getNameWithOwner() + "因为没有目标而无法发动效果！");
            }else {
                if (this instanceof AreaCard && ownerPlayer().isCanFanfare()){
                    info.msg(getNameWithOwner() + "发动战吼");
                    getPlay().effect().accept(choice,targets);
                }else
                    getPlay().effect().accept(choice,targets);
            }
        }
        // endregion 发动卡牌效果


        // 触发手牌上全部增幅效果
        String boostCards = ownerPlayer().getHandCopy().stream().map(card -> card.getEffects(EffectTiming.Boost))
            .flatMap(Collection::stream)
            .filter(boost -> boost.getCanEffect().test(this))
            .map(effect -> {
                effect.getEffect().accept(this);
                return effect.getOwnerObj().getId();
            }).collect(Collectors.joining("、"));
        if(!boostCards.isEmpty()){
            Msg.send(ownerPlayer().getSession(), boostCards + "发动增幅效果");
        }

        ownerPlayer().count(PLAY_NUM);
        ownerPlayer().count(PLAY_NUM_ALL);
        ownerPlayer().getPlayedCard().add(this);

        // region 计算使用后
        ownerLeader().useEffects(EffectTiming.AfterPlay,this);
        enemyLeader().useEffects(EffectTiming.AfterEnemyPlay,this);
        ownerPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.AfterPlay,this));
        enemyPlayer().getAreaCopy().forEach(areaCard -> areaCard.useEffects(EffectTiming.AfterEnemyPlay,this));
        // endregion 计算使用后
        info.startEffect();
        info.pushInfo();

    }

}
