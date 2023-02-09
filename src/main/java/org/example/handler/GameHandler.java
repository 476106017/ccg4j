package org.example.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.example.card.*;
import org.example.game.*;
import org.example.system.Lists;
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

        String room = userRoom.get(me);
        if(room==null)return;
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

        player.setStep(0);
        if(info.anotherPlayerByUuid(me).getStep()==0){
            String turnPlayerName = info.getPlayerInfos()[info.getTurnPlayer()].getName();
            // 两名玩家都换完了，开始游戏
            info.msg("双方均交换完成，游戏开始！由【"+turnPlayerName+"】先攻。");
            info.beginGame();
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
        String room = userRoom.get(me);
        if(room==null)return;
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

        String room = userRoom.get(me);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        // endregion

        if(!me.equals(player.getUuid())){
            info.msgTo(me,"当前不是你的回合！");
            return;
        }
        if(player.getStep() == 2){
            info.msgTo(me,"请先发现卡牌！（输入discover <序号>）");
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
        // 已选好要出的card
        Play play = card.getPlay();

        if(play==null){
            card.play(new ArrayList<>(),0);// 没有任何使用效果，直接召唤
            return;
        }

        // region 获取选择目标
        int choice = 0;
        List<GameObj> targets = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            String targetS = split[i];
            if (targetS.startsWith("s")) {
                if(play.choiceNum()==0){
                    info.msgToThisPlayer("此卡不需要抉择！");
                    return;
                }
                if(choice!=0){
                    info.msgToThisPlayer("输入了多个抉择序号:"+choice);
                    return;
                }
                try {
                    choice = Integer.parseInt(targetS.substring(1, 2));
                    if(choice<=0 || choice > play.choiceNum()){
                        info.msgToThisPlayer("指定抉择序号错误！应为：s1-s"+play.choiceNum());
                        return;
                    }
                } catch (Exception e) {
                }
                continue;
            }

            Integer targetI;
            try {
                targetI = Integer.valueOf(targetS);
                // 获取选择对象
                GameObj target = play.canTargets().get()
                    .get(i-1).get(targetI - 1);
                if(target!=null){
                    if(targets.contains(target)){
                        info.msgToThisPlayer("输入了重复的对象:"+target.getName());
                        return;
                    }
                    targets.add(target);
                }
            }catch (Exception e){}
        }
        // endregion 获取选择目标

        // 不指定目标
        if(targets.size() == 0){
            if(play.targetNum()==0 || ( play.targetNum()>0 && !play.mustTarget()))
                card.play(new ArrayList<>(),choice);// 不指定目标
            else
                info.msgToThisPlayer("请指定目标：play <手牌序号> <目标序号> s<抉择序号>\n"+play.describeCanTargets());
        }else{
            // 指定目标
            if(play.targetNum()==0) {
                info.msgToThisPlayer("无法为该卡牌指定目标！");
            }else {
                // 必须指定目标
                if(play.mustTarget() && targets.size() != play.targetNum()){
                    info.msgToThisPlayer("指定目标数量错误！应为："+ play.targetNum());
                    return;
                }
                card.play(targets,choice);// 指定目标
            }

        }
    }
    @OnEvent(value = "attack")
    public void attack(SocketIOClient client, String msg){

        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = userRoom.get(me);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        PlayerInfo enemy = info.oppositePlayer();
        // endregion

        if(!me.equals(player.getUuid())){
            info.msgTo(me,"当前不是你的回合！");
            return;
        }

        if(player.getStep() == 2){
            info.msgTo(me,"请先发现卡牌！（输入discover <序号>）");
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
            info.msgToThisPlayer("无法让护符卡攻击:"+amuletCard.getId());
            return;
        } else if (myCard instanceof FollowCard followCard) {
            if(followCard.hasKeyword("缴械")){
                info.msgToThisPlayer("被缴械的随从无法攻击！");
                return;
            }
            if(followCard.hasKeyword("眩晕")){
                info.msgToThisPlayer("眩晕的随从无法攻击！");
                return;
            }
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
            info.msgToThisPlayer("无法攻击护符卡:"+amuletCard.getId());
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
    @OnEvent(value = "discover")
    public void discover(SocketIOClient client, String msg){

        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = userRoom.get(me);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        PlayerInfo enemy = info.oppositePlayer();
        // endregion
        if(!me.equals(player.getUuid())){
            info.msgTo(me,"当前不是你的回合！");
            return;
        }
        if(player.getStep() != 2){
            info.msgTo(me,"当前状态无法发现卡牌！");
            return;
        }

        try {
            int indexI = Integer.parseInt(msg);
            player.setDiscoverNum(indexI);
            player.getDiscoverThread().start();
        }catch (Exception e){
            info.msgToThisPlayer("输入discover <序号>以发现一张卡牌");
        }
    }

    @OnEvent(value = "skill")
    public void skill(SocketIOClient client, String msg){
        // region 获取游戏对象
        UUID me = client.getSessionId();
        String name = userNames.get(me);
        String room = userRoom.get(me);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        PlayerInfo enemy = info.oppositePlayer();
        // endregion

        if(!me.equals(player.getUuid())){
            info.msgTo(me,"当前不是你的回合！");
            return;
        }
        if(player.getStep() == 2){
            info.msgTo(me,"请先发现卡牌！（输入discover <序号>）");
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
                            .append(targetCard.getId()).append("\t");
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
        info.pushInfo();
    }

    @OnEvent(value = "grave")
    public void grave(SocketIOClient client, String msg){

        UUID me = client.getSessionId();

        String room = userRoom.get(me);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.playerByUuid(me);

        info.msgTo(me, player.describeGraveyard());
    }

    // TODO 测试用，顺序出牌
    @OnEvent(value = "test")
    public void test(SocketIOClient client, String msg){
        UUID me = client.getSessionId();

        String room = userRoom.get(me);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();


        player.getHand().stream().filter(card -> {
                if(card.getCost() > player.getPpNum())return false;
                Play play = card.getPlay();
                if(play !=null && play.mustTarget()){// 如果是必须指定目标，每个可指定列表都不能为空
                    return play.canTargets().get().stream().noneMatch(List::isEmpty);
                }
                return true;
            })
            .findAny().ifPresent(card -> {
            if(card instanceof AreaCard &&
                player.getArea().size()==player.getAreaMax()){
                info.msgTo(me,"场上放不下卡牌了！");
                return;
            }
            Play play = card.getPlay();

            if(play==null){
                card.play(new ArrayList<>(),0);// 没有任何使用效果，直接召唤
                return;
            }
            // 所有效果都随机指定
            List<GameObj> targets = play.canTargets().get().stream().map(Lists::randOf).toList();
            int randChoice = (int) (play.choiceNum()* Math.random()+1);

            int temp = player.getDiscoverMax();
            player.setDiscoverMax(1);// 发现效果取随机1张
            card.play(targets,randChoice);
            player.setDiscoverMax(temp);
        });
    }
}
