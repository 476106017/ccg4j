package org.example.card;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.game.GameObj;
import org.example.game.GameInfo;
import org.example.game.PlayerInfo;
import org.example.system.function.FunctionN;
import org.example.system.function.PredicateN;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.example.constant.CounterKey.ALL_COST;
import static org.example.constant.CounterKey.PLAY_NUM;

@EqualsAndHashCode(callSuper = true)
@Data
public abstract class Card extends GameObj {

    @EqualsAndHashCode.Exclude
    protected GameInfo info = null;
    private int owner = 0;

    private Card parentCard = null;

    private Map<String,Integer> counter = new HashMap<>();

    private Set<String> keywords = new HashSet<>();

    public void addKeyword(String k){
        info.msg(getNameWithOwner()+"获得了【"+k+"】");
        getKeywords().add(k);
    }
    public boolean hasKeyword(String k){
        return getKeywords().contains(k);
    }

    // region 效果列表

    private List<Event.Play> plays = new ArrayList<>();

    public List<GameObj> getTargets(){
        return getPlays().stream().map(play -> play.targets.get()).flatMap(Collection::stream).distinct().toList();
    }
    private List<Event.InvocationBegin> invocationBegins = new ArrayList<>();
    private List<Event.InvocationEnd> invocationEnds = new ArrayList<>();
    private List<Event.Transmigration> transmigrations = new ArrayList<>();
    private List<Event.Boost> boosts = new ArrayList<>();
    private List<Event.Charge> charges = new ArrayList<>();
    private List<Event.WhenKill> whenKills = new ArrayList<>();
    // endregion 效果列表


    public PlayerInfo ownerPlayer(){
        return info.getPlayerInfos()[owner];
    }
    public PlayerInfo oppositePlayer(){
        return info.getPlayerInfos()[1-owner];
    }

    public abstract String getType();
    public abstract Integer getCost();
    public String getNameWithOwner(){
        return ownerPlayer().getName()+"的"+getName();
    };
    public abstract String getJob();
    public abstract List<String> getRace();
    public abstract String getMark();
    public abstract String getSubMark();

    public boolean atArea(){
        return ownerPlayer().getArea().contains(this);
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

    public void initCounter(){}

    public <T extends Card> T createCard(Class<T> clazz,String... keywords){
        T card = createCard(clazz);
        for (String keyword : keywords) {
            card.addKeyword(keyword);
        }
        return card;
    }
    public <T extends Card> T createCard(Class<T> clazz){
        try {
            T card = clazz.getDeclaredConstructor().newInstance();
            info.msg(getNameWithOwner()+"创造了"+card.getName());
            card.setParentCard(this); ;
            card.setOwner(owner);
            card.setInfo(info);
            card.initCounter();
            return card;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Card copyCard(){
        try {
            Card card = this.getClass().getDeclaredConstructor().newInstance();
            card.parentCard = this;
            card.owner = this.owner;
            card.info = this.info;
            card.initCounter();
            return card;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void play(List<GameObj> targets){
        if(ownerPlayer().getPpNum() < getCost()){
            info.msgToThisPlayer("你没有足够的PP来使用该卡牌！");
            throw new RuntimeException();
        }
        info.msg(ownerPlayer().getName() + "使用了" + getName());
        ownerPlayer().count(PLAY_NUM);

        // region 消耗PP
        int ppNum = ownerPlayer().getPpNum() - getCost();
        ownerPlayer().setPpNum(ppNum);
        ownerPlayer().count(ALL_COST,getCost());
        // endregion 消耗PP

        // region 驻场卡召唤到场上，法术卡丢到墓地
        if(this instanceof AreaCard areaCard){
            ownerPlayer().summon(areaCard);
            ownerPlayer().getHand().remove(areaCard);
        } else if (this instanceof SpellCard) {
            ownerPlayer().getGraveyard().add(this);
            ownerPlayer().countToGraveyard(1);
            ownerPlayer().getHand().remove(this);
        }
        // endregion

        // region 发动卡牌效果
        if (this instanceof AreaCard && !getPlays().isEmpty()) {
            info.msg(getNameWithOwner() + "发动战吼");
        }
        getPlays().forEach(play -> {
            // 如果指定目标全是该效果可选目标，目标数量也相等，则发动（多种指定效果可能冲突）
            if(play.targets.get().containsAll(targets) && play.targetNum == targets.size()){
                play.effect.accept(targets);
            }
        });
        // endregion 发动卡牌效果

        // 触发手牌上全部增幅效果
        ownerPlayer().getHand().stream().map(Card::getBoosts)
            .flatMap(Collection::stream)
            .filter(boost -> boost.canBeTriggered.test(this))
            .forEach(boost->boost.effect.accept(this));
    }

    public static class Event {
        /** 使用（到场上叫做战吼，法术牌释放效果） */
        public record Play(Supplier<List<GameObj>> targets, int targetNum, Consumer<List<GameObj>> effect){}
        /** 回合开始瞬召 */
        public record InvocationBegin(PredicateN canBeTriggered, FunctionN effect){}
        /** 回合结束瞬召 */
        public record InvocationEnd(PredicateN canBeTriggered, FunctionN effect){}
        /** 轮回(由墓地进入牌堆) */
        public record Transmigration(FunctionN effect){}
        /** 增幅(其他卡牌被使用) */
        public record Boost(Predicate<Card> canBeTriggered, Consumer<Card> effect){}
        /** 注能(场上卡牌被破坏) */
        public record Charge(Predicate<Card> canBeTriggered, Consumer<Card> effect){}
        /** 击杀时效果 */
        public record WhenKill(Consumer<FollowCard> effect){}
    }
}
