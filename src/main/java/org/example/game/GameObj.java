package org.example.game;


import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class GameObj {

    private static int id_iter=10000; //共用的静态变量
    public final int id;

    public transient GameInfo info;

    public transient int owner = 0;
    public void changeOwner(){
        owner = 1-owner;
    }
    public PlayerInfo ownerPlayer(){
        return info.getPlayerInfos()[owner];
    }
    public Leader ownerLeader(){
        return ownerPlayer().getLeader();
    }
    public PlayerInfo enemyPlayer(){
        return info.getPlayerInfos()[1-owner];
    }
    public Leader enemyLeader(){
        return enemyPlayer().getLeader();
    }

    public abstract void setName(String name);
    public abstract String getName();
    public String getId(){
        return getName();
    }
//    public String getId(){
//        return getName() + "#" + hashCode()%10000;
//    }

    public String getNameWithOwner(){
        if(info==null) return getId();
        return ownerPlayer().getName()+"的"+getId();
    };
    public void init(){}


    // region 效果操作
    public transient List<Effect> effects = new ArrayList<>();

    public void addEffects(Effect effect){
        effects.add(effect);
    }

    public List<Effect> getEffects(EffectTiming timing){
        return getEffects().stream().filter(effect -> effect.getTiming().equals(timing)).toList();
    }
    public List<Effect> getEffectsFrom(GameObj parent){
        return getEffects().stream().filter(effect -> effect.getParent().equals(parent)).toList();
    }
    /**
     * 检查对象是否已绑定 GameInfo
     * @param operation 要执行的操作名称
     * @throws IllegalStateException 如果未绑定 GameInfo
     */
    protected void checkInfoBound(String operation) {
        if (getInfo() == null) {
            throw new IllegalStateException(
                String.format("Cannot perform %s on unbound object %s", 
                    operation, getClass().getSimpleName()));
        }
    }

    // 不加入队列，立即生效的效果（增加回复量、伤害量、加减状态等）
    public void useEffects(EffectTiming timing, Object param){
        checkInfoBound("useEffects");
        List<Effect> effectList = getEffects(timing);
        if(effectList.size()>0)
            ownerPlayer().count(timing.getName());
        effectList.forEach(effect -> {
            new Effect.EffectInstance(effect,param).consume();
        });
    }
    public void useEffects(EffectTiming timing){
        checkInfoBound("useEffects");
        List<Effect> effectList = getEffects(timing);
        if(effectList.size()>0)
            ownerPlayer().count(timing.getName());
        effectList.forEach(effect -> {
            new Effect.EffectInstance(effect,null).consume();
        });
    }
    public void useEffectsAndSettle(EffectTiming timing){
        useEffectsAndSettle(timing,null);
    }
    public void useEffectsAndSettle(EffectTiming timing, Object param){
        tempEffects(timing,param);
        info.startEffect();
    }
    public void tempEffects(EffectTiming timing){
        checkInfoBound("tempEffects");
        List<Effect> effectList = getEffects(timing);
        if(effectList.size()>0)
            ownerPlayer().count(timing.getName());
        effectList.forEach(effect -> {
            if(effect.getCanEffect().test(null))
                info.tempEffect(new Effect.EffectInstance(effect));
        });
    }
    public void tempEffects(EffectTiming timing,Object param){
        checkInfoBound("tempEffects");
        List<Effect> effectList = getEffects(timing);
        if(effectList.size()>0)
            ownerPlayer().count(timing.getName());
        effectList.forEach(effect -> {
            if(effect.getCanEffect().test(param))
                info.tempEffect(new Effect.EffectInstance(effect,param));
        });
    }
    // endregion 效果操作

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
    public <T extends Card> T createCard(Class<T> clazz, int atk,int hp){
        T card = createCard(clazz);
        if(card instanceof FollowCard followCard){
            followCard.setAtk(atk);
            followCard.setMaxHp(hp);
            followCard.setHp(hp);
        }
        return card;
    }
    public <T extends Card> T createCard(Class<T> clazz){
        try {
            T card = clazz.getDeclaredConstructor().newInstance();
            try {
                info.msg(getNameWithOwner()+"创造了"+card.getId());
            }catch (Exception ignored){}
            card.setParent(this);
            card.setOwner(getOwner());
            card.setInfo(getInfo());
            card.init();
            info.useAreaCardEffectBatch(ownerPlayer().getAreaCopy(),EffectTiming.WhenCreateCard,card);
            ownerLeader().useEffects(EffectTiming.WhenCreateCard,card);
            return card;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public <T extends Card> T createEnemyCard(Class<T> clazz, String... keywords){
        T card = createEnemyCard(clazz);
        for (String keyword : keywords) {
            card.addKeyword(keyword);
        }
        return card;
    }
    public <T extends Card> T createEnemyCard(Class<T> clazz){
        try {
            T card = clazz.getDeclaredConstructor().newInstance();
            info.msg(getNameWithOwner()+"创造了"+card.getId());
            card.setParent(this);
            card.changeOwner();
            card.setInfo(getInfo());
            card.init();
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
