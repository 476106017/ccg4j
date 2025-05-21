package org.example.game;

import jakarta.websocket.Session;
import lombok.Getter;
import lombok.Setter;
import org.example.card.*;
import org.example.constant.EffectTiming;
import org.example.system.util.CardPackage;
import org.example.system.util.Lists;
import org.example.system.util.Maps;
import org.example.system.util.Msg;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.PLAY_NUM;
import static org.example.constant.CounterKey.POISON;

import org.example.system.GameStateService;

@Getter
@Setter
public class GameInfo implements Serializable {
    private final GameStateService gameStateService;
    String room;

    // 连锁
    boolean canChain = true;
    int chainDeep = 3;
    boolean inSettle = false;
    int turn;
    int turnPlayer;
    int moreTurn = 0;// 追加回合
    boolean gameset = false;
    ScheduledFuture<?> rope;
    List<Damage> incommingDamages = new ArrayList<>();
    Map<Card,EventType> events = new HashMap<>();
    List<Effect.EffectInstance> effectInstances = new LinkedList<>();

    public boolean hasEvent(){
        return !incommingDamages.isEmpty() || !events.isEmpty();
    }

    public void setCanChain(boolean canChain) {
        if (canChain)
            msg("本场游戏已启用连锁");
        else
            msg("本场游戏已禁用连锁");

        this.canChain = canChain;
    }

    PlayerInfo[] playerInfos;

    public GameInfo(String room, GameStateService gameStateService) {
        this.room = room;
        this.gameStateService = gameStateService;
        this.turn = 1;
        this.turnPlayer = 0;
        this.playerInfos = new PlayerInfo[2];
        this.playerInfos[0] = new PlayerInfo(this,true);
        this.playerInfos[1] = new PlayerInfo(this,false);
    }

    public void resetGame(){
        msg("游戏重启！");
        this.gameStateService.getRoomSchedule().get(getRoom()).shutdown();
        this.gameStateService.getRoomSchedule().remove(getRoom());
        rope.cancel(true);
        this.turn = 1;
        this.turnPlayer = 0;
        Session thisSession = thisPlayer().session;
        Session oppoSession = oppositePlayer().session;

        PlayerDeck deck0 = this.gameStateService.getUserDecks().get(thisSession);
        String name0 = this.gameStateService.getUserNames().get(thisSession);
        PlayerDeck deck1 = this.gameStateService.getUserDecks().get(oppoSession);
        String name1 = this.gameStateService.getUserNames().get(oppoSession);

        this.playerInfos = new PlayerInfo[2];
        this.playerInfos[0] = new PlayerInfo(this,true);
        this.playerInfos[1] = new PlayerInfo(this,false);
        zeroTurn(deck0, name0, thisSession, deck1, name1, oppoSession);
    }

    public void msg(String msg){
        try {
            Msg.send(thisPlayer().getSession(),msg);
            Msg.send(oppositePlayer().getSession(),msg);
        } catch (Exception ignored) {}
    }
    public void story(String msg){
        try {
            Msg.story(thisPlayer().getSession(),msg);
            Msg.story(oppositePlayer().getSession(),msg);
        } catch (Exception ignored) {}
    }

    public void msgTo(Session session, String msg){
        Msg.send(session,msg);
    }

    public void pushInfo(){
        final PlayerInfo thisPlayer = thisPlayer();
        thisPlayer.setDeckCount(thisPlayer.getDeck().size());
        final PlayerInfo oppositePlayer = oppositePlayer();
        oppositePlayer.setDeckCount(oppositePlayer.getDeck().size());
        // region 加载补充信息
        thisPlayer.getAreaAsCard().forEach(f->f.setSubMarkStr(f.getSubMark()));
        thisPlayer.getHand().forEach(f->f.setSubMarkStr(f.getSubMark()));
        oppositePlayer.getAreaAsCard().forEach(f->f.setSubMarkStr(f.getSubMark()));
        // endregion 加载补充信息
        thisPlayer.getAreaAsCard().forEach(f->f.setSubMarkStr(f.getSubMark()));
        thisPlayer.getAreaFollowsAsFollow().forEach(f->{
            // 回合可攻击数没有打满
            final boolean notAttacked = f.getTurnAttack() < f.getTurnAttackMax();
            // 状态正常
            final boolean normalStatus = !f.hasKeyword("缴械") && !f.hasKeyword("眩晕") && !f.hasKeyword("冻结");
            final boolean canAttack = notAttacked && normalStatus &&
                (f.getTurnAge() > 0 || f.hasKeyword("疾驰"));
            final boolean canDash = notAttacked && normalStatus &&
                (f.getTurnAge() == 0 && !f.hasKeyword("疾驰") && f.hasKeyword("突进"));

            f.setCanAttack(canAttack);
            f.setCanDash(canDash);
        });


        Msg.send(thisPlayer.getSession(),"battleInfo",
            Maps.newMap("me", thisPlayer,"enemy", oppositePlayer));
        Msg.send(oppositePlayer.getSession(),"battleInfo",
            Maps.newMap("me", oppositePlayer,"enemy", thisPlayer));
    }

    public void msgToThisPlayer(String msg){
        Msg.send(thisPlayer().getSession(),msg);
    }
    public void msgToOppositePlayer(String msg){
        Msg.send(oppositePlayer().getSession(),msg);
    }

    public void measureLeader(){
        if(thisPlayer().getHp()<=0)
            gameset(oppositePlayer());
        if(oppositePlayer().getHp()<=0)
            gameset(thisPlayer());
    }
    public void measureFollows(){
//        msg("——————结算卡牌状态——————");
        // 立即结算受伤状态
        List<Damage> incommingDamagesCopy = new ArrayList<>(incommingDamages);
        incommingDamages = new ArrayList<>();
        incommingDamagesCopy.forEach(damage->{
            damage.getTo().useEffects(EffectTiming.AfterDamaged,damage);
        });

        Map<Card, EventType> eventsCopy = events;
        events = new HashMap<>();
        // 再结算其他状态
        eventsCopy.forEach((card, type) -> {
            switch (type){
                case Destroy -> {
                    if(card instanceof AreaCard areaCard) areaCard.destroyed();
                }
            }
        });

        assert events.isEmpty();
    }

    public void gameset(PlayerInfo winner){
        gameset = true;
        msg("游戏结束，获胜者："+winner.getName());
        pushInfo();
        final Session winnerSession = winner.getSession();
        Msg.send(winnerSession,"alert","你赢了！");
        Msg.send(anotherPlayerBySession(winnerSession).getSession(),"alert","你输了！");

        // 释放资源
        this.gameStateService.getRoomGame().remove(getRoom());
        // 退出房间
        try {
            this.gameStateService.getUserRoom().remove(thisPlayer().getSession());
            msgToThisPlayer("离开房间成功");
            this.gameStateService.getUserRoom().remove(oppositePlayer().getSession());
            msgToOppositePlayer("离开房间成功");

            rope.cancel(true);
            ScheduledExecutorService ses = this.gameStateService.getRoomSchedule().get(getRoom());
            ses.shutdown();
            this.gameStateService.getRoomSchedule().remove(getRoom());
        }catch (Exception e){e.printStackTrace();}
        throw new RuntimeException("Game Set");
    }

    public PlayerInfo thisPlayer(){
        return playerInfos[turnPlayer];
    }
    public PlayerInfo oppositePlayer(){
        return playerInfos[1-turnPlayer];
    }
    public PlayerInfo playerBySession(Session session){
        if(playerInfos[0].session == session){
            return playerInfos[0];
        }else {
            return playerInfos[1];
        }
    }
    public PlayerInfo anotherPlayerBySession(Session session){
        if(playerInfos[0].session == session){
            return playerInfos[1];
        }else {
            return playerInfos[0];
        }
    }

    // region effect
    public boolean addEvent(Card card,EventType type){
        EventType oldType = events.get(card);
        if(oldType != null){
//            msg(card.getNameWithOwner() + "已经被" + oldType.getName() + "，无法再被" + type.getName());
            return false;
        }
//        msg(card.getNameWithOwner() + "的" + type.getName() + "状态已加入队列");
        events.put(card,type);
        return true;
    }
    public void useAreaCardEffectBatch(List<AreaCard> cards, EffectTiming timing){
        List<GameObj> gameObjs = cards.stream().map(p -> (GameObj) p).toList();
        tempEffectBatch(gameObjs,timing);
        startEffect();
    }
    public void useAreaCardEffectBatch(List<AreaCard> cards, EffectTiming timing,Object param){
        List<GameObj> gameObjs = cards.stream().map(p -> (GameObj) p).toList();
        tempEffectBatch(gameObjs,timing,param);
        startEffect();
    }
    public void useEffectBatch(List<GameObj> objs, EffectTiming timing){
        tempEffectBatch(objs,timing);
        startEffect();
    }

    public void tempEffectBatch(List<GameObj> objs, EffectTiming timing,Object param){
        objs.forEach(obj -> obj.tempEffects(timing,param));
    }
    public void tempEffectBatch(List<GameObj> objs, EffectTiming timing){
        objs.forEach(obj -> obj.tempEffects(timing));
    }
    public void tempCardEffectBatch(List<Card> objs, EffectTiming timing){
        objs.forEach(obj -> obj.tempEffects(timing));
    }
    public void tempCardEffectBatch(List<Card> objs, EffectTiming timing, Object param) {
        objs.forEach(obj -> obj.tempEffects(timing,param));
    }

    public void tempAreaCardEffectBatch(List<AreaCard> objs, EffectTiming timing){
        objs.forEach(obj -> obj.tempEffects(timing));
    }
    public void tempAreaCardEffectBatch(List<AreaCard> objs, EffectTiming timing,Object param){
        objs.forEach(obj -> obj.tempEffects(timing,param));
    }
    public void tempEffect(Effect.EffectInstance instance){
        Effect effect = instance.getEffect();
        effectInstances.add(instance);
//        msg(effect.getOwnerObj().getNameWithOwner()+"的【"+effect.getTiming().getName()+"】效果已加入队列" +
//            "（队列现在有" + effectInstances.size() + "个效果）");
    }

    // 结算效果
    public void startEffect(){

        if(inSettle)return;
        inSettle = true;
//        msg("——————开始结算——————");

        consumeEffectChain(chainDeep);
        // 计算主战者死亡状况
        measureLeader();
        inSettle = false;
    }
    public void consumeEffectChain(int deep){
//        msg("——————开始触发事件——————");
        measureFollows();
//        msg("——————开始触发效果——————");
        consumeEffect();
//        msg("——————停止触发效果——————");

        if(hasEvent()){
            if(!canChain || deep==0){
                msg("停止连锁！本次死亡结算后不触发任何效果");
                measureFollows();
                effectInstances.clear();
                events.clear();
                return;
            }
//            msg("——————事件连锁（"+deep+"）——————");
            consumeEffectChain(deep - 1);
        }
    }
    public void consumeEffect(){
        if(effectInstances.isEmpty()) return;
        effectInstances.sort((o1, o2) -> {
            for (EffectTiming value : EffectTiming.values()) {
                if(value.equals(o1.getEffect().getTiming()))
                    return -1;
                else if(value.equals(o2.getEffect().getTiming()))
                    return 1;
            }
            return 0;
        });

        List<Effect.EffectInstance> instances = new ArrayList<>(effectInstances);

        instances.forEach(Effect.EffectInstance::consume);

        effectInstances.clear();
    }
    // endregion effect


    // region event

    public void transform(Card fromCard, Card toCard){
        msg(fromCard.getNameWithOwnerWithPlace()+ "变身成了" + toCard.getId());
        if(fromCard.atArea()){
            if(fromCard.hasKeyword("魔法免疫")){
                fromCard.getInfo().msg(fromCard.getNameWithOwner() + "免疫了本次变身！");
                return;
            }
            if(fromCard.hasKeyword("魔法护盾")){
                fromCard.getInfo().msg(fromCard.getNameWithOwner() + "的魔法护盾抵消了本次变身！");
                fromCard.removeKeyword("魔法护盾");
                return;
            }
            List<AreaCard> area = fromCard.ownerPlayer().getArea();
            int index = area.indexOf(fromCard);
            area.remove(index);
            fromCard.useEffects(EffectTiming.WhenNoLongerAtArea);
            // 要变成随从
            if (toCard instanceof AreaCard areaCard) {
                area.add(index, areaCard);
                areaCard.useEffects(EffectTiming.WhenAtArea);
            } else {
                msg(toCard.getNameWithOwner()+ "无法留在战场而被除外！");
                exile(toCard);
            }

        }else {
            List<Card> where = fromCard.where();
            int index = where.indexOf(fromCard);
            where.remove(index);
            where.add(index,toCard);
        }
    }
    public void exile(Card card){
        exile(List.of(card));
    }
    public void exile(List<Card> cards){
        if(cards.isEmpty())return;
        msg(cards.stream().map(Card::getNameWithOwner).collect(Collectors.joining("、"))+ "从游戏中除外！");
        cards.forEach(card ->{
            if(card.where()==null)return;


            // 场上卡除外时，有机会发动离场时效果
            if (card.atArea() && card instanceof AreaCard areaCard){
                if(areaCard.hasKeyword("魔法免疫")){
                    areaCard.getInfo().msg(areaCard.getNameWithOwner() + "免疫了本次除外！");
                    return;
                }
                if(areaCard.hasKeyword("魔法护盾")){
                    areaCard.getInfo().msg(areaCard.getNameWithOwner() + "的魔法护盾抵消了本次除外！");
                    areaCard.removeKeyword("魔法护盾");
                    return;
                }
                card.removeWhenAtArea();
                card.tempEffects(EffectTiming.Leaving);
                // 场上随从除外时，装备也除外
                if(card instanceof FollowCard followCard && followCard.equipped())
                    exile(followCard.getEquipment());
            }else
                card.removeWhenNotAtArea();


            card.tempEffects(EffectTiming.Exile);
            if(card.hasKeyword("恶魔转生")){
                List<Card> totalCard = new ArrayList<>();
                totalCard.addAll(thisPlayer().getHand().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(thisPlayer().getArea().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(thisPlayer().getGraveyard().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(thisPlayer().getDeck().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(oppositePlayer().getHand().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(oppositePlayer().getArea().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(oppositePlayer().getGraveyard().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());
                totalCard.addAll(oppositePlayer().getDeck().stream()
                    .filter(c -> c instanceof FollowCard f
                        && !f.hasKeyword("恶魔转生")).toList());

                if(totalCard.isEmpty()){
                    msg("游戏中只剩下恶魔牌，"+card.getNameWithOwner()+"已经无法转生");
                    return;
                }
                Card luckyCard = Lists.randOf(totalCard);

                Card newCard = card.createCard(card.getClass());
                transform(luckyCard,newCard);
            }
        });
        startEffect();
    }
    public void damageMulti(GameObj from,List<GameObj> objs, int damage){
        List<Damage> damages = objs.stream().map(obj -> new Damage(from, obj, damage)).toList();
        new DamageMulti(this,damages).apply();
    }
    public void damageAttacking(FollowCard from, GameObj to){
        if(to instanceof FollowCard && !from.hasKeyword("远程") && !((FollowCard) to).hasKeyword("眩晕"))
            new DamageMulti(this,List.of(new Damage(from,to), new Damage(to,from))).apply();
        else
            new DamageMulti(this,List.of(new Damage(from,to))).apply();
    }
    public void damageEffect(GameObj from,GameObj to, int damage){
        new DamageMulti(this,List.of(new Damage(from,to,damage))).apply();
    }

    // endregion event

    public List<AreaCard> getAreaCardsCopy(){
        List<AreaCard> _result = new ArrayList<>();
        _result.addAll(thisPlayer().getArea());
        _result.addAll(oppositePlayer().getArea());
        return _result;
    }
    public List<GameObj> getTargetableGameObj(){
        List<GameObj> _result = new ArrayList<>();
        _result.addAll(thisPlayer().getAreaFollows());
        _result.addAll(oppositePlayer().getAreaFollows());
        _result.add(thisPlayer().getLeader());
        _result.add(oppositePlayer().getLeader());
        return _result;
    }
    public List<AreaCard> getAreaFollowsCopy(){
        List<AreaCard> _result = new ArrayList<>();
        _result.addAll(thisPlayer().getAreaFollows());
        _result.addAll(oppositePlayer().getAreaFollows());
        return _result;
    }
    public List<GameObj> getAreaFollowsAsGameObj(){
        List<GameObj> _result = new ArrayList<>();
        _result.addAll(thisPlayer().getAreaFollows());
        _result.addAll(oppositePlayer().getAreaFollows());
        return _result;
    }

    // region turn
    public void beginGame(){
        Leader leader = thisPlayer().getLeader();
        leader.setCanUseSkill(true);
        leader.useEffects(EffectTiming.BeginGame);

        Leader enemyLeader = oppositePlayer().getLeader();
        enemyLeader.useEffects(EffectTiming.BeginGame);

        Msg.send(thisPlayer().getSession(),"swapOver","");
        Msg.send(oppositePlayer().getSession(),"swapOver","");

    }
    public void zeroTurn(PlayerDeck deck0, String name0, Session session0, PlayerDeck deck1, String name1, Session session1){

        PlayerInfo p0 = thisPlayer();
        p0.setSession(session0);
        p0.setName(name0);
        p0.setLeader(deck0.getLeader(0, this));
        p0.setDeck(deck0.getActiveDeckInstance(0, this));
        Collections.shuffle(p0.getDeck());

        PlayerInfo p1 = oppositePlayer();
        p1.setSession(session1);
        p1.setName(name1);
        p1.setLeader(deck1.getLeader(1, this));
        p1.setDeck(deck1.getActiveDeckInstance(1, this));
        Collections.shuffle(p1.getDeck());

        p0.getLeader().init();
        p1.getLeader().init();

        p0.draw(3);
        p1.draw(3);
        msg("游戏开始，请选择3张手牌交换");
        Msg.send(p0.getSession(),"swap",p0.getHand());
        Msg.send(p1.getSession(),"swap",p1.getHand());
    }


    public void startTurn(){
        thisPlayer().clearCount(PLAY_NUM);
        thisPlayer().getPlayedCard().clear();
        if(thisPlayer().ppMax<thisPlayer().getPpLimit()){
                thisPlayer().ppMax++;
        }
        thisPlayer().ppNum = thisPlayer().ppMax;
        msg("第" + turn + "回合：" + thisPlayer().getName()+"的回合，有" + thisPlayer().ppNum + "pp");
        beforeTurn();
        thisPlayer().draw(1);

        if(thisPlayer().isShortRope()){
            rope = this.gameStateService.getRoomSchedule().get(getRoom()).schedule(this::endTurnOfTimeout, 30, TimeUnit.SECONDS);
            msg("倒计时30秒！");
        }else{
            rope = this.gameStateService.getRoomSchedule().get(getRoom()).schedule(this::endTurnOfTimeout, 300, TimeUnit.SECONDS);
            msg("倒计时300秒！");
        }
        pushInfo();
        msgToThisPlayer("请出牌！");
        msgToOppositePlayer("等待对手出牌......");
        Msg.send(thisPlayer().getSession(),"yourTurn","");
        Msg.send(oppositePlayer().getSession(),"enemyTurn","");


        if(turn==10){// TODO 活动模式，第10回合奖励
            final List<Class<? extends Card>> classes = CardPackage.randCard("passive", 3);
            final List<Card> list =  classes.stream().map(clazz -> (Card)thisPlayer().getLeader().createCard(clazz)).toList();
            thisPlayer().discoverCard(list,card -> card.getPlay().effect().accept(0,new ArrayList<>()));
        }
    }

    public void endTurnOfTimeout(){
        thisPlayer().setShortRope(true);
        endTurn();
    }
    public void endTurnOfCommand(){
        thisPlayer().setShortRope(false);
        rope.cancel(true);
        endTurn();
    }

    public void endTurn(){
        thisPlayer().autoDiscover();
        msg(thisPlayer().getName()+"的回合结束");
        try {
            afterTurn();
        }catch (Exception e){
            e.printStackTrace();
        }

        if(thisPlayer().getStep() == -1)return;// 回合结束效果触发了重启游戏
        // 是否有追加回合
        if(moreTurn>0){
            moreTurn--;
        }else {
            turn += turnPlayer;// 如果是玩家1就加回合数
            turnPlayer = 1 ^ turnPlayer;
        }
        msg("——————————");

        startTurn();
    }

    public void beforeTurn(){


        // 场上随从驻场回合+1、攻击次数清零
        // 发动回合开始效果
        // 场上护符倒数-1
        oppositePlayer().getAreaCopy().forEach(enemyAreaCard -> {
            if(!enemyAreaCard.atArea())return;

            enemyAreaCard.useEffects(EffectTiming.EnemyBeginTurn);
            if(!enemyAreaCard.atArea())return;


            if(enemyAreaCard instanceof FollowCard followCard && followCard.equipped()){
                EquipmentCard equipment = followCard.getEquipment();
                equipment.useEffects(EffectTiming.EnemyBeginTurn);
            }

            if(enemyAreaCard instanceof FollowCard followCard){
                followCard.setTurnAttack(0);
                followCard.removeKeyword("眩晕");
                followCard.removeKeyword("冻结");
//                followCard.removeKeywordAll("格挡");
            }
        });
        thisPlayer().getAreaCopy().forEach(areaCard -> {
            if(!areaCard.atArea())return;

            areaCard.useEffects(EffectTiming.BeginTurn);
            if(!areaCard.atArea())return;


            if(areaCard instanceof FollowCard followCard && followCard.equipped()){
                EquipmentCard equipment = followCard.getEquipment();
                equipment.useEffects(EffectTiming.BeginTurn);
            }
            if(!areaCard.atArea())return;

            if(areaCard instanceof FollowCard followCard){
                int turnAgePlus = followCard.getTurnAge() + 1;
                followCard.setTurnAge(turnAgePlus);
                followCard.setTurnAttack(0);
            }

            if(areaCard instanceof AmuletCard amuletCard){
                int countDown = amuletCard.getCountDown();
                if(countDown > 0){
                    amuletCard.countDown();
                }
            }
        });

        // 查找牌堆是否有瞬召卡牌，同名字卡牌各取一张
        Map<String, GameObj> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o, (a,b)->a));

        // 瞬召卡牌
        useEffectBatch(new ArrayList<>(nameCard.values()),EffectTiming.InvocationBegin);

        // 主战者技能重置、发动主战者效果和手牌效果
        Leader leader = thisPlayer().getLeader();
        leader.setCanUseSkill(true);
        leader.useEffects(EffectTiming.BeginTurn);
        thisPlayer().getHandCopy().forEach(card -> card.useEffects(EffectTiming.BeginTurnAtHand));

        Leader enemyLeader = oppositePlayer().getLeader();
        enemyLeader.useEffects(EffectTiming.EnemyBeginTurn);
        oppositePlayer().getHandCopy().forEach(card -> card.useEffects(EffectTiming.EnemyBeginTurnAtHand));
    }
    public void afterTurn(){
        // 对手中毒效果
        final Integer poison = oppositePlayer().getCount(POISON);
        if(poison>0){
            msg(oppositePlayer().getLeader().getNameWithOwner() + "受到"+poison+"点中毒伤害");
            damageEffect(thisPlayer().getLeader(), oppositePlayer().getLeader(), poison);
            oppositePlayer().count(POISON,-1);
        }

        oppositePlayer().getAreaCopy().forEach(areaCard -> {
            if(areaCard instanceof FollowCard followCard && followCard.hasKeyword("中毒")){
                final int poison1 = followCard.countKeyword("中毒");
                msg(followCard.getNameWithOwner() + "受到"+poison1+"点中毒伤害");
                damageEffect(followCard, followCard, poison1);
            }
        });

        // 发动回合结束效果
        oppositePlayer().getAreaCopy().forEach(areaCard -> {
            if(!areaCard.atArea())return;

            areaCard.useEffects(EffectTiming.EnemyEndTurn);
            if(!areaCard.atArea())return;


            if(areaCard instanceof FollowCard followCard && followCard.equipped()){
                EquipmentCard equipment = followCard.getEquipment();
                equipment.useEffects(EffectTiming.EnemyEndTurn);
            }
        });
        thisPlayer().getAreaCopy().forEach(areaCard -> {
            if(!areaCard.atArea())return;

            areaCard.useEffects(EffectTiming.EndTurn);
            if(!areaCard.atArea())return;


            if(areaCard instanceof FollowCard followCard && followCard.equipped()){
                EquipmentCard equipment = followCard.getEquipment();
                equipment.useEffects(EffectTiming.EndTurn);
            }
        });
        thisPlayer().getHandCopy().forEach(card -> {
           if(card.hasKeyword("虚无")){
               thisPlayer().abandon(card);
           }
        });

        // 查找牌堆是否有瞬召卡牌，同名字卡牌各取一张
        Map<String, GameObj> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o, (a,b)->a));

        // 瞬召卡牌
        useEffectBatch(new ArrayList<>(nameCard.values()),EffectTiming.InvocationEnd);

        // 发动主战者效果
        Leader leader = thisPlayer().getLeader();
        leader.useEffects(EffectTiming.EndTurn);
        leader.expireEffect();
        thisPlayer().getHandCopy().forEach(card -> card.useEffects(EffectTiming.EndTurnAtHand));
        thisPlayer().setHandPlayable(card -> true);

        Leader enemyLeader = oppositePlayer().getLeader();
        enemyLeader.useEffects(EffectTiming.EnemyEndTurn);
        enemyLeader.expireEffect();
        oppositePlayer().getHandCopy().forEach(card -> card.useEffects(EffectTiming.EnemyEndTurnAtHand));
    }

    public void addMoreTurn(){
        moreTurn++;
    }
    // endregion turn
}
