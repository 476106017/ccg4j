package org.example.game;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import lombok.Getter;
import lombok.Setter;
import org.example.card.*;
import org.example.constant.EffectTiming;
import org.example.system.Lists;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.example.constant.CounterKey.PLAY_NUM;
import static org.example.system.Database.*;

@Getter
@Setter
public class GameInfo {
    SocketIOServer server;
    String room;

    // 连锁
    boolean canChain = true;
    int chainDeep = 3;
    boolean inSettle = false;
    int turn;
    int turnPlayer;
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

    public GameInfo(SocketIOServer server, String room) {
        this.room = room;
        this.server = server;
        this.turn = 1;
        this.turnPlayer = 0;
        this.playerInfos = new PlayerInfo[2];
        this.playerInfos[0] = new PlayerInfo(this,true);
        this.playerInfos[1] = new PlayerInfo(this,false);

    }

    public void resetGame(){
        msg("游戏重启！");
        roomSchedule.get(getRoom()).shutdown();
        roomSchedule.remove(getRoom());
        this.turn = 1;
        this.turnPlayer = 0;
        UUID thisUUid = thisPlayer().uuid;
        UUID oppoUUid = oppositePlayer().uuid;
        this.playerInfos = new PlayerInfo[2];
        this.playerInfos[0] = new PlayerInfo(this,true);
        this.playerInfos[1] = new PlayerInfo(this,false);
        zeroTurn(thisUUid,oppoUUid);
    }

    public void msg(String msg){
        server.getRoomOperations(room).sendEvent("receiveMsg", msg);
    }

    public void msgTo(UUID uuid, String msg){
        server.getClient(uuid).sendEvent("receiveMsg", msg);
    }

    public void pushInfo(){
        //
        try {
            server.getClient(thisPlayer().getUuid())
                .sendEvent("battleInfo", describeGame(thisPlayer().getUuid()));
        }catch (Exception ignored){}
        try {
            server.getClient(oppositePlayer().getUuid())
            .sendEvent("battleInfo", describeGame(oppositePlayer().getUuid()));
        }catch (Exception ignored){}
    }

    public void msgToThisPlayer(String msg){
        server.getClient(thisPlayer().getUuid()).sendEvent("receiveMsg", msg);
    }
    public void msgToOppositePlayer(String msg){
        server.getClient(oppositePlayer().getUuid()).sendEvent("receiveMsg", msg);
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

        // 释放资源
        roomReadyMatch.remove(getRoom());
        roomGame.remove(getRoom());
        // 退出房间
        try {
            // 如果找不到玩家，就算了
            try {
                userRoom.remove(thisPlayer().getUuid());
                SocketIOClient client = server.getClient(thisPlayer().getUuid());
                client.leaveRoom(getRoom());
                client.sendEvent("receiveMsg","离开房间成功");
            }catch (Exception ignored){}
            try {
                userRoom.remove(oppositePlayer().getUuid());
                SocketIOClient client = server.getClient(oppositePlayer().getUuid());
                client.leaveRoom(getRoom());
                client.sendEvent("receiveMsg","离开房间成功");
            }catch (Exception ignored){}

            ScheduledExecutorService ses = roomSchedule.get(getRoom());
            ses.shutdown();
            ses.close();
            roomSchedule.remove(getRoom());
        }catch (Exception e){e.printStackTrace();}
        throw new RuntimeException("Game Set");
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
    public void useCardEffectBatch(List<Card> cards, EffectTiming timing){
        List<GameObj> gameObjs = cards.stream().map(p -> (GameObj) p).toList();
        tempEffectBatch(gameObjs,timing);
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

    public void zeroTurn(UUID u0, UUID u1){

        PlayerInfo p0 = thisPlayer();
        PlayerDeck playerDeck0 = userDecks.get(u0);
        p0.setUuid(u0);
        p0.setName(userNames.get(u0));
        p0.setLeader(playerDeck0.getLeader(0, this));
        p0.setDeck(playerDeck0.getActiveDeckInstance(0, this));
        Collections.shuffle(p0.getDeck());

        PlayerInfo p1 = oppositePlayer();
        p1.setUuid(u1);
        p1.setName(userNames.get(u1));
        p1.setLeader(playerDeck0.getLeader(1, this));
        p1.setDeck(userDecks.get(u1).getActiveDeckInstance(1, this));
        Collections.shuffle(p1.getDeck());

        p0.draw(3);
        p1.draw(3);
        msgToThisPlayer("你的手牌:\n"+p0.describeHand());
        msgToOppositePlayer("你的手牌:\n"+p1.describeHand());
        msg("游戏开始，请选择3张手牌交换");
    }

    // region turn
    public void beginGame(){
        Leader leader = thisPlayer().getLeader();
        leader.setCanUseSkill(true);
        leader.useEffects(EffectTiming.BeginGame);

        Leader enemyLeader = oppositePlayer().getLeader();
        enemyLeader.useEffects(EffectTiming.BeginGame);
    }

    public void startTurn(){
        thisPlayer().clearCount(PLAY_NUM);
        if(thisPlayer().ppMax<10){
                thisPlayer().ppMax++;
        }
        thisPlayer().ppNum = thisPlayer().ppMax;
        thisPlayer().draw(1);
        msg("第" + turn + "回合：" + thisPlayer().getName()+"的回合，有" + thisPlayer().ppNum + "pp");
        beforeTurn();

        if(thisPlayer().isShortRope()){
            rope = roomSchedule.get(getRoom()).schedule(this::endTurnOfTimeout, 30, TimeUnit.SECONDS);
            msg("倒计时30秒！");
        }else{
            rope = roomSchedule.get(getRoom()).schedule(this::endTurnOfTimeout, 300, TimeUnit.SECONDS);
            msg("倒计时300秒！");
        }
        pushInfo();
        msgToThisPlayer("请出牌！");
        msgToOppositePlayer("等待对手出牌......");
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
        msg(thisPlayer().getName()+"的回合结束");
        try {
            afterTurn();
        }catch (Exception e){
            e.printStackTrace();
        }

        if(thisPlayer().getStep() == -1)return;// 回合结束效果触发了重启游戏
        turn += turnPlayer;// 如果是玩家1就加回合数
        turnPlayer = 1 ^ turnPlayer;
        msg("——————————");

        startTurn();
    }

    public void beforeTurn(){
        // 主战者技能重置、发动主战者效果和手牌效果
        Leader leader = thisPlayer().getLeader();
        leader.setCanUseSkill(true);
        leader.useEffects(EffectTiming.BeginTurn);
        thisPlayer().getHandCopy().forEach(card -> card.useEffects(EffectTiming.BeginTurnAtHand));

        Leader enemyLeader = oppositePlayer().getLeader();
        enemyLeader.useEffects(EffectTiming.EnemyBeginTurn);
        oppositePlayer().getHandCopy().forEach(card -> card.useEffects(EffectTiming.EnemyBeginTurnAtHand));


        // 场上随从驻场回合+1、攻击次数清零
        // 发动回合开始效果
        // 场上护符倒数-1
        oppositePlayer().getAreaCopy().forEach(areaCard -> {
            if(!areaCard.atArea())return;

            areaCard.useEffects(EffectTiming.EnemyBeginTurn);
            if(!areaCard.atArea())return;


            if(areaCard instanceof FollowCard followCard && followCard.equipped()){
                EquipmentCard equipment = followCard.getEquipment();
                equipment.useEffects(EffectTiming.EnemyBeginTurn);
            }

            if(areaCard instanceof FollowCard followCard){
                followCard.setTurnAttack(0);
                followCard.removeKeyword("眩晕");
                followCard.removeKeywordAll("格挡");
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
                if(followCard.notAttacked()){
                    msg(followCard.getNameWithOwner() + "可以攻击了！");
                }

                followCard.setTurnAttack(0);
            }

            if(areaCard instanceof AmuletCard amuletCard){
                int countDown = amuletCard.getCountDown();
                if(countDown > 0){
                    amuletCard.setCountDown(countDown - 1);
                    msg(amuletCard.getNameWithOwner() + "的倒数-1");
                    if(amuletCard.getCountDown() == 0){
                        amuletCard.death();
                    }
                }
            }
        });

        // 查找牌堆是否有瞬召卡牌，同名字卡牌各取一张
        Map<String, GameObj> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o, (a,b)->a));

        // 瞬召卡牌
        useEffectBatch(new ArrayList<>(nameCard.values()),EffectTiming.InvocationBegin);

    }
    public void afterTurn(){
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

        // 查找牌堆是否有瞬召卡牌，同名字卡牌各取一张
        Map<String, GameObj> nameCard =
            thisPlayer().getDeck().stream().collect(Collectors.toMap(Card::getName, o -> o, (a,b)->a));

        // 瞬召卡牌
        useEffectBatch(new ArrayList<>(nameCard.values()),EffectTiming.InvocationEnd);
    }

    // endregion turn

    // region describe
    public String describeArea(UUID uuid){
        StringBuilder sb = new StringBuilder();
        PlayerInfo player = playerByUuid(uuid);
        PlayerInfo oppositePlayer = anotherPlayerByUuid(uuid);

        sb.append("【战场信息】\n");
        sb.append("敌方战场：\n");
        for (int i = 0; i < oppositePlayer.getArea().size(); i++) {
            sb.append("<p>");
            Card card = oppositePlayer.getArea().get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getId()).append("\t");
            if("随从".equals(card.getType())){
                FollowCard follow = (FollowCard) card;
                if(follow.notAttacked()){
                    sb.append("未攻击").append("\t");
                }
                sb.append(follow.getAtk()).append("/").append(follow.getHp())
                    .append("\t").append(follow.getMaxHp()==follow.getHp()?"满":"残").append("\t");
                if(follow.getEquipment()!=null){
                    sb.append("装备中：").append(follow.getEquipment().getId());
                    if(follow.getEquipment().getCountdown()!=-1)
                        sb.append("（").append(follow.getEquipment().getCountdown()).append("）");
                    sb.append("\t");
                }
            }
            if("护符".equals(card.getType())){
                AmuletCard amulet = (AmuletCard) card;
                if(amulet.getCountDown()>0){
                    sb.append("倒数：").append(amulet.getCountDown()).append("\t");
                }
            }

            if(!card.getKeywords().isEmpty())
                sb.append(card.getKeywords());


            // region 显示详情
            StringBuilder detail = new StringBuilder();
            if(card instanceof FollowCard followCard)
                detail.append(followCard.getAtk()).append("➹")
                    .append(followCard.getHp()).append("♥");
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
            <icon class="glyphicon glyphicon-eye-open" style="font-size:18px;"
                    title="%s" data-content="%s" data-placement="auto"
                    data-container="body" data-toggle="popover"
                      data-trigger="hover" data-html="true"/>
            """.formatted(card.getName(),detail.toString().replaceAll("\\n","<br/>")));
            // endregion
            sb.append("</p>");
        }
        sb.append("\n我方战场：\n");
        for (int i = 0; i < player.getArea().size(); i++) {
            sb.append("<p>");
            Card card = player.getArea().get(i);
            sb.append("【").append(i+1).append("】\t")
                .append(card.getType()).append("\t")
                .append(card.getId()).append("\t");
            if("随从".equals(card.getType())){
                FollowCard follow = (FollowCard) card;
                if(follow.notAttacked()){
                    sb.append("未攻击").append("\t");
                }
                sb.append(follow.getAtk()).append("/").append(follow.getHp())
                    .append("\t").append(follow.getMaxHp()==follow.getHp()?"满":"残").append("\t");
                if(follow.getEquipment()!=null){
                    sb.append("装备中：").append(follow.getEquipment().getId());
                    if(follow.getEquipment().getCountdown()!=-1)
                        sb.append("（").append(follow.getEquipment().getCountdown()).append("）");
                    sb.append("\t");
                }
            }
            if("护符".equals(card.getType())){
                AmuletCard amulet = (AmuletCard) card;
                if(amulet.getCountDown()>0){
                    sb.append("倒数：").append(amulet.getCountDown()).append("\t");
                }
            }

            if(!card.getKeywords().isEmpty())
                sb.append(card.getKeywords());
            // region 显示详情
            StringBuilder detail = new StringBuilder();
            if(card instanceof FollowCard followCard)
                detail.append(followCard.getAtk()).append("➹")
                    .append(followCard.getHp()).append("♥");
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
            <icon class="glyphicon glyphicon-eye-open" style="font-size:18px;"
                    title="%s" data-content="%s" data-placement="auto"
                    data-container="body" data-toggle="popover"
                      data-trigger="hover" data-html="true"/>
            """.formatted(card.getName(),detail.toString().replaceAll("\\n","<br/>")));
            // endregion
            sb.append("</p>");
        }
        sb.append("\n");

        return sb.toString();
    }


    public String describeGame(UUID uuid){
        if(gameset)return "无法获取游戏信息";

        StringBuilder sb = new StringBuilder();
        PlayerInfo player = playerByUuid(uuid);
        PlayerInfo oppositePlayer = anotherPlayerByUuid(uuid);

        sb.append("【主战者信息】\n");
        sb.append("玩家【").append(oppositePlayer.name)
            .append("】\t血：").append(oppositePlayer.getHp()).append("/").append(oppositePlayer.getHpMax())
            .append("\n牌：").append(oppositePlayer.deck.size())
            .append("\t墓：").append(oppositePlayer.graveyardCount)
            .append("\t手：").append(oppositePlayer.hand.size())
            .append("\n");
        sb.append("玩家【").append(player.name)
            .append("】\t血：").append(player.getHp()).append("/").append(player.getHpMax())
            .append("\n牌：").append(player.deck.size())
            .append("\t墓：").append(player.graveyardCount)
            .append("\t手：").append(player.hand.size())
            .append("\n");

        sb.append("\n");

        sb.append(describeArea(uuid));

        sb.append("我的手牌：\n").append(player.describeHand());

        return sb.toString();
    }
    // endregion describe
}
