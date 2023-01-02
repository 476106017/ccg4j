package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.card.*;
import org.example.game.GameInfo;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.PlayerInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;

import static org.example.system.Database.*;

@Service
@ConditionalOnClass(SocketIOServer.class)
@Slf4j
public class GameHandler {
    @Autowired
    SocketIOServer socketIOServer;

    @Autowired
    Gson gson;

    @OnEvent(value = "swap")
    public void swap(SocketIOClient client, String msg) {
        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.playerByUuid(me);
        // endregion

        // region 交换
        Set<Integer> indexs = new HashSet<>();
        for (String index : msg.split("\\s+")) {
            if(index.isEmpty()) continue;
            Integer indexI;
            try {
                indexI = Integer.valueOf(index);
            }catch (Exception e){
                indexI = -1;
            }
            if(indexI<1 || indexI>3){
                info.msgTo(me,"输入序号错误:"+index);
                return;
            }
            indexs.add(indexI-1);//这里转成下标
        }
        if(player.getStep()!=-1){
            info.msgTo(me,"已经过了换牌步骤!");
            return;
        }
        if(indexs.isEmpty()){
            info.msg(name +"交换了0张卡牌");
        }else{
            List<Card> hand = player.getHand();
            List<Card> back = new ArrayList<>();
            for (Integer index : indexs) {
                back.add(hand.get(index));
            }
            player.draw(back.size());
            player.backToDeck(back);
            info.msg(name +"交换了"+back.size()+"张卡牌");
        }
        // endregion

        info.msgTo(me,"交换后的手牌:\n"+player.describeHand());

        player.setStep(0);
        if(info.anotherPlayerByUuid(me).getStep()==0){
            String turnPlayerName = info.getPlayerInfos()[info.getTurnPlayer()].getName();
            // 两名玩家都换完了，开始游戏
            info.msg("双方均交换完成，游戏开始！由【"+turnPlayerName+"】先攻。");

            roomSchedule.put(room, Executors.newScheduledThreadPool(1));// 房间里面放一个计时器
            info.startTurn();
        }else {
            info.msgTo(me,"请等待对方换牌");
        }
    }

    /* 回合结束 */
    @OnEvent(value = "end")
    public void turnEnd(SocketIOClient client, String msg){

        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        // endregion

        if(!me.equals(player.getUuid())){
            if("f".equals(msg)){
                info.msgTo(me,"强制结束对手回合！");
                info.endTurnOfCommand();
            }else {
                info.msgTo(me,"当前不是你的回合！");
            }
            return;
        }

        info.endTurnOfCommand();
    }

    /* 出牌 */
    @OnEvent(value = "play")
    public void play(SocketIOClient client, String msg) {
        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        // endregion

        if(!me.equals(player.getUuid())){
            info.msgTo(me,"当前不是你的回合！");
            return;
        }

        if(msg.isBlank()){
            info.msgToThisPlayer("打出卡牌：play <手牌序号> <目标序号> s<抉择序号>；");
            return;
        }

        String[] split = msg.split("\\s+");

        Integer indexI;
        try {
            indexI = Integer.valueOf(split[0]);
            split[0] = "";
        }catch (Exception e){
            indexI = -1;
        }
        if(indexI <= 0 || indexI > player.getHand().size()){
            info.msgToThisPlayer("输入手牌序号错误:"+split[0]);
            return;
        }

        Card card = player.getHand().get(indexI - 1);
        if(card instanceof AreaCard && !(card instanceof EquipmentCard) &&
            player.getArea().size()==player.getAreaMax()){
            info.msgToThisPlayer("场上放不下卡牌了！");
            return;
        }

        List<GameObj> targetable = card.getTargets();

        Integer targetNum = card.getPlays().stream()
            .map(Card.Event.Play::targetNum).reduce(Math::max).orElse(0);
        // 只有一个参数
        if(split.length == 1){
            if(targetable.isEmpty()
                && !(card instanceof SpellCard && targetNum>0)// 指定法术卡必须指定
                && !(card instanceof EquipmentCard)){// 装备卡必须指定
                // 如果是站场卡，则不指定也能召唤
                card.play(targetable,0);// 不指定目标
            }else {
                StringBuilder sb = new StringBuilder();
                sb.append("你需要指定目标以使用该卡牌：play <手牌序号> <目标序号> s<抉择序号>\n")
                    .append("可指定的目标：\n");
                for (int i = 0; i < targetable.size(); i++) {
                    sb.append("【").append(i+1).append("】\t");

                    GameObj gameObj = targetable.get(i);
                    if(gameObj instanceof Leader leader){
                        sb.append(player.getLeader()==leader?"我方主战者":"敌方主战者");
                    }else if (gameObj instanceof Card targetCard){
                        // 卡牌属于哪方
                        PlayerInfo ownerPlayer = targetCard.ownerPlayer();
                        sb.append(ownerPlayer == player ? "我方\t":"敌方\t")
                            .append(targetCard.getName()).append("\t");
                        if(gameObj instanceof FollowCard followCard){
                            sb.append("随从\t")
                                .append(followCard.getAtk()).append("/").append(followCard.getHp());
                        }else if(gameObj instanceof AmuletCard amuletCard){
                            sb.append("护符\t")
                                .append("倒数：").append(amuletCard.getCountDown());
                        }
                    }
                    sb.append("\n");
                }
                info.msgToThisPlayer(sb.toString());

            }
        }else{
            // 输入多个参数
            if(targetable.size()==0) {
                info.msgToThisPlayer("无法为该卡牌指定目标！");
                return;
            }else {
                List<GameObj> targets = new ArrayList<>();
                int choice = 0;
                for (String targetS : split) {
                    if(targetS.startsWith("s")){
                        try {
                            choice = Integer.parseInt(targetS.substring(1,2));
                        }catch (Exception e){
                            choice = 0;
                        }
                        continue;
                    }
                    Integer targetI;
                    try {
                        targetI = Integer.valueOf(targetS);
                    }catch (Exception e){
                        targetI = 0;
                    }
                    try {
                        GameObj target = targetable.get(targetI - 1);
                        if(target!=null){
                            targets.add(target);
                        }
                    }catch (Exception e){}
                }
                int shouldTargetNum = Math.min(targetable.size(), targetNum);
                if(targets.size() != shouldTargetNum){
                    info.msgToThisPlayer("指定目标数量错误！应为："+shouldTargetNum);
                    return;
                }
                Integer choiceNum = card.getPlays().stream().map(Card.Event.Play::choiceNum).reduce(Math::min).orElse(0);
                // 需要指定抉择时
                if (choiceNum > 0)
                    if (choice > choiceNum || choice <= 0) {
                        info.msgToThisPlayer("指定抉择序号错误！应为：s1-s" + choiceNum);
                        return;
                    }else
                        info.msg(player.getName()+"进行了抉择");
                card.play(targets,choice);// 指定目标（装备卡只有1个目标）
            }

        }


    }
    @OnEvent(value = "attack")
    public void attack(SocketIOClient client, String msg){

        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        PlayerInfo enemy = info.oppositePlayer();
        // endregion

        if(!me.equals(player.getUuid())){
            info.msgTo(me,"当前不是你的回合！");
            return;
        }

        String[] split = msg.split("\\s+");

        if(msg.isBlank() || split.length!=2){
            info.msgToThisPlayer("攻击：attack <随从序号> <目标随从序号(敌方主战者序号是0)>");
            return;
        }


        Integer indexI;
        try {
            indexI = Integer.valueOf(split[0]);
        }catch (Exception e){
            indexI = -1;
        }
        if(indexI <= 0 || indexI > player.getArea().size()){
            info.msgToThisPlayer("输入随从序号错误:"+split[0]);
            return;
        }
        Card myCard = player.getArea().get(indexI - 1);
        if(myCard instanceof AmuletCard amuletCard){
            info.msgToThisPlayer("无法让护符卡攻击:"+amuletCard.getName());
            return;
        } else if (myCard instanceof FollowCard followCard) {
            if(followCard.getTurnAge() == 0 &&
                !followCard.hasKeyword("突进") &&
                !followCard.hasKeyword("疾驰")){
                info.msgToThisPlayer("无法让刚入场的随从攻击");
                return;
            }
            if(followCard.getTurnAttack() == followCard.getTurnAttackMax()){
                info.msgToThisPlayer("该随从已经攻击过了");
                return;
            }
        }

        Integer indexII;
        try {
            indexII = Integer.valueOf(split[1]);
        }catch (Exception e){
            indexII = -1;
        }
        if(indexII < 0 || indexII > enemy.getArea().size()){
            info.msgToThisPlayer("输入目标序号错误:"+split[1]);
            return;
        } else if (indexII == 0) {
            FollowCard myFollow = (FollowCard) myCard;
            if(myFollow.getTurnAge() == 0 && !myFollow.hasKeyword("疾驰")){
                info.msgToThisPlayer("随从在入场回合无法攻击敌方主战者！");
                return;
            }
            Optional<AreaCard> guard = enemy.getArea().stream().filter(areaCard -> areaCard.hasKeyword("守护")).findAny();
            if(guard.isPresent() && !myFollow.hasKeyword("无视守护")){
                info.msgToThisPlayer("你必须先攻击带有守护效果的随从！");
                return;
            }
            info.msg(myFollow.getNameWithOwner()+"直接攻击对手的主战者");
            myFollow.attack(enemy.getLeader());
            return;
        } else if(enemy.getArea().get(indexII-1) instanceof AmuletCard amuletCard){
            info.msgToThisPlayer("无法攻击护符卡:"+amuletCard.getName());
            return;
        }
        FollowCard myFollow = (FollowCard) myCard;
        FollowCard target = (FollowCard)enemy.getArea().get(indexII-1);

        Optional<AreaCard> guard = enemy.getArea().stream().filter(areaCard -> areaCard.hasKeyword("守护")).findAny();
        if(!target.hasKeyword("守护") && guard.isPresent() && !myFollow.hasKeyword("无视守护")){
            info.msgToThisPlayer("你必须先攻击带有守护效果的随从！");
            return;
        }
        myFollow.attack(target);


    }

    @OnEvent(value = "skill")
    public void skill(SocketIOClient client, String msg){
        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        PlayerInfo enemy = info.oppositePlayer();
        // endregion

        if(!me.equals(player.getUuid())){
            info.msgTo(me,"当前不是你的回合！");
            return;
        }

        Leader leader = player.getLeader();
        List<GameObj> targetable = leader.targetable();
        if(msg.isBlank()){
            if(!leader.isNeedTarget()){
                leader.skill(null);
            }else {
                StringBuilder sb = new StringBuilder();
                sb.append("你需要指定目标以使用" + leader.getSkillName() + "：skill <目标序号>\n")
                    .append("可指定的目标：\n");
                if(targetable.isEmpty()) sb.append("没有可指定的目标！\n");
                for (int i = 0; i < targetable.size(); i++) {
                    sb.append("【").append(i+1).append("】\t");

                    GameObj gameObj = targetable.get(i);
                    if(gameObj instanceof Leader leader1){
                        sb.append(player.getLeader()==leader1?"我方主战者":"敌方主战者");
                    }else if (gameObj instanceof Card targetCard){
                        // 卡牌属于哪方
                        PlayerInfo ownerPlayer = targetCard.ownerPlayer();
                        sb.append(ownerPlayer == player ? "我方\t":"敌方\t")
                            .append(targetCard.getName()).append("\t");
                        if(gameObj instanceof FollowCard followCard){
                            sb.append("随从\t")
                                .append(followCard.getAtk()).append("/").append(followCard.getHp());
                        }else if(gameObj instanceof AmuletCard amuletCard){
                            sb.append("护符\t");
                            sb.append("倒数：").append(amuletCard.getCountDown()).append("\t");
                        }
                    }
                    sb.append("\n");
                }
                info.msgToThisPlayer(sb.toString());
            }
        }else {// 输入了指定对象
            if(!leader.isNeedTarget()){
                info.msgToThisPlayer("不可指定目标！");
                return;
            }
            GameObj target = null;
            try {
                int indexI = Integer.parseInt(msg);
                target = targetable.get(indexI-1);
            }catch (Exception e){
                info.msgToThisPlayer("指定目标错误！");
                return;
            }
            leader.skill(target);
        }

    }


    @OnEvent(value = "info")
    public void info(SocketIOClient client, String msg){

        UUID me = client.getSessionId();
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);

        info.msgTo(me, info.describeGame(me));
    }
    @OnEvent(value = "area")
    public void area(SocketIOClient client, String msg){

        UUID me = client.getSessionId();
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);

        info.msgTo(me, info.describeArea(me));
    }
    @OnEvent(value = "hand")
    public void hand(SocketIOClient client, String msg){

        UUID me = client.getSessionId();
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.playerByUuid(me);

        info.msgTo(me, player.describeHand());
    }
    @OnEvent(value = "grave")
    public void grave(SocketIOClient client, String msg){

        UUID me = client.getSessionId();
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.playerByUuid(me);

        info.msgTo(me, player.describeGraveyard());
    }
    @OnEvent(value = "ff")
    public void ff(SocketIOClient client, String msg){

        UUID me = client.getSessionId();
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.playerByUuid(me);
        PlayerInfo enemy = info.anotherPlayerByUuid(me);

        info.msg(player.getName() + "宣告投降！");
        info.gameset(enemy);


    }
    @OnEvent(value = "leave")
    public void leave(SocketIOClient client, String msg){

        UUID me = client.getSessionId();
        Optional<String> roomOpt = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny();
        if(roomOpt.isEmpty()){
            client.sendEvent("receiveMsg","你不在任何房间中");
        }
        String room = roomOpt.get();
        GameInfo info = roomGame.get(room);
        if(info!=null){
            PlayerInfo player = info.playerByUuid(me);
            PlayerInfo enemy = info.anotherPlayerByUuid(me);
            info.msg(player.getName() + "离开了游戏！");
            info.gameset(enemy);
            return;
        }
        client.leaveRoom(room);
        client.sendEvent("receiveMsg","离开房间成功");
        // 释放资源
        roomReadyMatch.remove(room);
        roomGame.remove(room);
        if(me.equals(waitUser) || room.equals(waitRoom) ){
            waitRoom = "";
            waitUser = null;
        }
        // 退出房间

    }

    // TODO 测试用，顺序出牌
    @OnEvent(value = "test")
    public void test(SocketIOClient client, String msg){
        UUID me = client.getSessionId();
        String room = client.getAllRooms().stream().filter(p -> !p.isBlank()).findAny().get();
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();


        player.getHand().stream().filter(card -> card.getCost()<= player.getPpNum())
            .findAny().ifPresent(card -> {
            if(card instanceof AreaCard &&
                player.getArea().size()==player.getAreaMax()){
                info.msgTo(me,"场上放不下卡牌了！");
                return;
            }

            if(card.getPlays().isEmpty()){
                card.play(new ArrayList<>(),1);
                return;
            }

            Integer targetNum = card.getPlays().stream().map(Card.Event.Play::targetNum).reduce(Math::max).get();
            int shouldTargetNum = Math.min(card.getTargets().size(), targetNum);
            if(shouldTargetNum == 0){
                if(card instanceof EquipmentCard) return;
                card.play(new ArrayList<>(),1);
            }else {
                List<GameObj> targets = card.getTargets().subList(0, shouldTargetNum);
                card.play(targets,1);
            }

        });
    }
}
