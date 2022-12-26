package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.card.AmuletCard;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
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
            player.back(back);
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
        }
    }

    /* 回合结束 */
    @OnEvent(value = "turn")
    public void turn(SocketIOClient client, String msg){

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


        if("end".equals(msg)){
            info.endTurnOfCommand();
        }
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
            info.msgTo(me,"打出卡牌：play <手牌序号> （<目标序号>）；");
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
            info.msgTo(me,"输入手牌序号错误:"+split[0]);
            return;
        }

        Card card = player.getHand().get(indexI - 1);
        if(card instanceof AreaCard &&
            player.getArea().size()==player.getAreaMax()){
            info.msgTo(me,"场上放不下卡牌了！");
            return;
        }
        List<GameObj> targetable = card.getTargets();
        // 只有一个参数
        if(split.length == 1){
            if(targetable.isEmpty()){
                card.play(targetable);
            }else {
                StringBuilder sb = new StringBuilder();
                sb.append("你需要指定目标以使用该卡牌：play <手牌序号> <目标序号>\n")
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
                                .append(amuletCard.getCount()).append("/").append(amuletCard.getTimer());
                        }
                    }
                    sb.append("\n");
                }
                info.msgTo(me, sb.toString());

            }
        }else{
            // 输入多个参数
            if(targetable.size()==0) {
                info.msgTo(me,"无法为该卡牌指定目标！");
                return;
            }else {
                List<GameObj> targets = new ArrayList<>();
                for (String targetS : split) {
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
                Integer targetNum = card.getPlays().stream().map(Card.Event.Play::targetNum).reduce(Math::max).get();
                int shouldTargetNum = Math.min(targetable.size(), targetNum);
                if(targets.size() != shouldTargetNum){
                    info.msgTo(me,"指定目标数量错误！应为："+shouldTargetNum);
                    return;
                }
                card.play(targets);
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
            info.msgToThisPlayer("攻击：attack <随从序号> <目标随从序号(对方主战者序号是0)>");
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
            info.msgTo(me, "输入目标序号错误:"+split[1]);
            return;
        } else if (indexII == 0) {
            // TODO 可否直接攻击
            FollowCard myFollow = (FollowCard) myCard;
            if(myFollow.getTurnAge() == 0 && !myFollow.hasKeyword("疾驰")){
                info.msgTo(me,"随从在入场回合无法攻击对方主战者");
                return;
            }
            info.msg(myFollow.getNameWithOwner()+"直接攻击对手的主战者！");
            myFollow.attack(enemy.getLeader());
            return;
        } else if(enemy.getArea().get(indexII-1) instanceof AmuletCard amuletCard){
            info.msgToThisPlayer("无法攻击护符卡:"+amuletCard.getName());
            return;
        }
        FollowCard myFollow = (FollowCard) myCard;
        FollowCard target = (FollowCard)enemy.getArea().get(indexII-1);
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
            if(targetable.isEmpty()){
                leader.skill(null);
            }else {
                StringBuilder sb = new StringBuilder();
                sb.append("你需要指定目标以使用" + leader.getSkillName() + "：skill <目标序号>\n")
                    .append("可指定的目标：\n");
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
                            sb.append("护符\t")
                                .append(amuletCard.getCount()).append("/").append(amuletCard.getTimer());
                        }
                    }
                    sb.append("\n");
                }
                info.msgTo(me, sb.toString());
            }
        }else {
            Integer indexI;
            try {
                indexI = Integer.valueOf(msg);
            }catch (Exception e){
                indexI = -1;
            }
            GameObj target = null;
            try {
                target = targetable.get(indexI-1);
            }catch (Exception e){
                info.msgTo(me,"指定目标错误！");
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
}
