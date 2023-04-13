package org.example.endpoint.handler;

import com.google.gson.Gson;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;
import org.example.card.*;
import org.example.game.*;
import org.example.system.util.Lists;
import org.example.system.util.Maps;
import org.example.system.util.Msg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;

import static org.example.system.Database.*;

@Service
@Slf4j
public class GameHandler {

    @Autowired
    Gson gson;


    public void swap(Session client, String msg) {
        // region 获取游戏对象
        String name = userNames.get(client);

        String room = userRoom.get(client);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.playerBySession(client);
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
                Msg.warn(client,"输入序号错误:"+index);
                return;
            }
            indexs.add(indexI-1);//这里转成下标
        }
        if(player.getStep() > -1){
            Msg.warn(client,"已经过了换牌步骤!");
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
        if(info.anotherPlayerBySession(client).getStep()==0){
            String turnPlayerName = info.getPlayerInfos()[info.getTurnPlayer()].getName();
            // 两名玩家都换完了，开始游戏
            info.msg("双方均交换完成，游戏开始！由【"+turnPlayerName+"】先攻。");
            info.beginGame();
            roomSchedule.put(room, Executors.newScheduledThreadPool(1));// 房间里面放一个计时器
            info.startTurn();
        }else {
            Msg.send(client,"请等待对方换牌");
        }
    }

    /* 回合结束 */

    public void end(Session client, String msg){

        // region 获取游戏对象
        String name = userNames.get(client);
        String room = userRoom.get(client);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        // endregion

        if(!client.equals(player.getSession())){
            if("f".equals(msg)){
                Msg.send(client,"强制结束对手回合！");
                info.endTurnOfCommand();
            }else {
                Msg.warn(client,"当前不是你的回合！");
            }
            return;
        }

        info.endTurnOfCommand();
    }

    /* 出牌 */

    public void play(Session client, String msg) {
        // region 获取游戏对象
        String name = userNames.get(client);

        String room = userRoom.get(client);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        // endregion

        boolean myTurn = client.equals(player.getSession());
        if(!myTurn){
            player = info.oppositePlayer();
        }

        if(player.getStep() > 0){
            Msg.warn(client,"请先发现卡牌！");
            return;
        }

        if(msg.isBlank()){
            Msg.warn(client,"打出卡牌：play <手牌序号> <目标id> s<抉择序号>；");
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
            Msg.warn(client,"输入手牌序号错误:"+split[0]);
            return;
        }

        Card card = player.getHand().get(indexI - 1);

        if(!myTurn && !card.hasKeyword("速攻")){
            Msg.warn(client,"当前不是你的回合！");
            return;
        }

        if(card instanceof AreaCard && !(card instanceof EquipmentCard) &&
            player.getArea().size()==player.getAreaMax()){
            Msg.warn(client,"场上放不下卡牌了！");
            return;
        }
        // 已选好要出的card
        Play play = card.getPlay();

        if(play==null){
            card.play(new ArrayList<>(),0);// 没有任何使用效果，直接召唤
            return;
        }

        // region 获取选择目标
        int choice = 1;
        List<GameObj> targets = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            String targetS = split[i];
            if (targetS.startsWith("s")) {
                if(play.choiceNum()==0){
                    Msg.warn(client,"此卡不需要抉择！");
                    return;
                }
                try {
                    choice = Integer.parseInt(targetS.substring(1, 2));
                    if(choice<=0 || choice > play.choiceNum()){
                        Msg.warn(client,"指定抉择序号错误！应为：s1-s"+play.choiceNum());
                        return;
                    }
                } catch (Exception e) {
                }
                continue;
            }

            Integer targetId;
            try {
                targetId = Integer.valueOf(targetS);
                // 获取选择对象
                Optional<GameObj> target = play.canTargets().get()
                    .get(i-1).stream().filter(gameObj -> gameObj.id==targetId).findFirst();
                if(target.isPresent()){
                    if(targets.contains(target)){
                        Msg.warn(client,"输入了重复的目标");
                        return;
                    }
                    targets.add(target.get());
                }
            }catch (Exception e){}
        }
        // endregion 获取选择目标

        // 不指定目标
        if(targets.size() == 0){
            if(play.targetNum()==0 )
                // 不需要指定目标
                card.play(new ArrayList<>(),choice);// 不指定目标
            else{
                // 可以指定目标
                final List<List<GameObj>> targetLists = play.canTargets().get();
                if(targetLists.stream().noneMatch(List::isEmpty)) {
                    // 多个目标列表没有空的，指定目标
                    Msg.send(client,"target",
                        Maps.newMap("pref",msg,"targetLists", targetLists));
                    Msg.warn(client,"请指定目标：play <手牌序号> <目标序号> s<抉择序号>\n"+play.describeCanTargets());
                }else{
                    if(play.mustTarget())//必须指定
                        Msg.warn(client,"现在无法打出这张卡牌！");
                    else// 不指定目标
                        card.play(new ArrayList<>(),choice);
                }
            }
        }else{
            // 指定目标
            if(play.targetNum()==0) {
                Msg.warn(client,"无法为该卡牌指定目标！");
            }else {
                // 必须指定目标
                if(play.mustTarget() && targets.size() != play.targetNum()){
                    Msg.warn(client,"指定目标数量错误！应为："+ play.targetNum());
                    info.pushInfo();
                    return;
                }
                card.play(targets,choice);// 指定目标
            }

        }
    }

    public void attack(Session client, String msg){

        // region 获取游戏对象
        String name = userNames.get(client);
        String room = userRoom.get(client);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        PlayerInfo enemy = info.oppositePlayer();
        // endregion

        if(!client.equals(player.getSession())){
            Msg.warn(client,"当前不是你的回合！");
            return;
        }

        if(player.getStep() > 0){
            Msg.warn(client,"请先发现卡牌！（输入discover <序号>）");
            return;
        }

        String[] split = msg.split("\\s+");

        if(msg.isBlank() || split.length!=2){
            Msg.warn(client,"攻击：attack <随从序号> <目标随从序号(敌方主战者序号是0)>");
            return;
        }


        Integer indexI;
        try {
            indexI = Integer.valueOf(split[0]);
        }catch (Exception e){
            indexI = -1;
        }
        if(indexI <= 0 || indexI > player.getArea().size()){
            Msg.warn(client,"输入随从序号错误:"+split[0]);
            return;
        }
        Card myCard = player.getArea().get(indexI - 1);
        if(myCard instanceof AmuletCard amuletCard){
            Msg.warn(client,"无法让护符卡攻击:"+amuletCard.getId());
            return;
        } else if (myCard instanceof FollowCard followCard) {
            if(followCard.hasKeyword("冻结")){
                Msg.warn(client,"被冻结的随从无法攻击！");
                return;
            }
            if(followCard.hasKeyword("缴械")){
                Msg.warn(client,"被缴械的随从无法攻击！");
                return;
            }
            if(followCard.hasKeyword("眩晕")){
                Msg.warn(client,"眩晕的随从无法攻击！");
                return;
            }
            if(followCard.getTurnAge() == 0 &&
                !followCard.hasKeyword("突进") &&
                !followCard.hasKeyword("疾驰")){
                Msg.warn(client,"无法让刚入场的随从攻击");
                return;
            }
            if(followCard.getTurnAttack() == followCard.getTurnAttackMax()){
                Msg.warn(client,"该随从已经攻击过了");
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
            Msg.warn(client,"输入目标序号错误:"+split[1]);
            return;
        } else if (indexII == 0) {
            FollowCard myFollow = (FollowCard) myCard;
            if(myFollow.getTurnAge() == 0 && !myFollow.hasKeyword("疾驰")){
                Msg.warn(client,"随从在入场回合无法攻击敌方主战者！");
                return;
            }
            Optional<AreaCard> guard = enemy.getArea().stream().filter(areaCard -> areaCard.hasKeyword("守护")).findAny();
            if(guard.isPresent() && !myFollow.hasKeyword("无视守护")){
                Msg.warn(client,"你必须先攻击带有守护效果的随从！");
                return;
            }
            info.msg(myFollow.getNameWithOwner()+"直接攻击对手的主战者");
            myFollow.attack(enemy.getLeader());
            return;
        } else if(enemy.getArea().get(indexII-1) instanceof AmuletCard amuletCard){
            Msg.warn(client,"无法攻击护符卡:"+amuletCard.getId());
            return;
        }
        FollowCard myFollow = (FollowCard) myCard;
        FollowCard target = (FollowCard)enemy.getArea().get(indexII-1);

        Optional<AreaCard> guard = enemy.getArea().stream().filter(areaCard -> areaCard.hasKeyword("守护")).findAny();
        if(!target.hasKeyword("守护") && guard.isPresent() && !myFollow.hasKeyword("无视守护")){
            Msg.warn(client,"你必须先攻击带有守护效果的随从！");
            return;
        }
        myFollow.attack(target);


    }

    public void discover(Session client, String msg){

        // region 获取游戏对象
        String name = userNames.get(client);
        String room = userRoom.get(client);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        PlayerInfo enemy = info.oppositePlayer();
        // endregion
        if(!client.equals(player.getSession())){
            Msg.warn(client,"当前不是你的回合！");
            return;
        }
        if(player.getStep() == 0){
            Msg.warn(client,"当前状态无法发现卡牌！");
            return;
        }

        try {
            int indexI = Integer.parseInt(msg);
            player.setDiscoverNum(indexI);
            player.getDiscoverThread().run();
            info.startEffect();
            if(player.getStep()==0)// 全部发现完再渲染
                info.pushInfo();
        }catch (Exception e){
            Msg.warn(client,"输入discover <序号>以发现一张卡牌");
        }
    }


    public void skill(Session client, String msg){
        // region 获取游戏对象
        String name = userNames.get(client);
        String room = userRoom.get(client);
        if(room==null)return;
        GameInfo info = roomGame.get(room);
        PlayerInfo player = info.thisPlayer();
        PlayerInfo enemy = info.oppositePlayer();
        // endregion

        if(!client.equals(player.getSession())){
            Msg.warn(client,"当前不是你的回合！");
            return;
        }
        if(player.getStep() > 0){
            Msg.warn(client,"请先发现卡牌！（输入discover <序号>）");
            return;
        }

        Leader leader = player.getLeader();
        List<GameObj> targetable = leader.targetable();
        if(msg.isBlank()){// 没有输入指定对象
            if (leader.isNeedTarget()) {
                if(targetable.isEmpty()){
                    Msg.warn(client,"现在无法使用主战者技能！");
                }else {
                    // 指定目标
                    Msg.send(client,"skill",targetable);
                    Msg.warn(client,"请指定目标");
                }
            } else {
                leader.skill(null);
                info.pushInfo();
            }
        }else {// 输入了指定对象
            if(!leader.isNeedTarget()){
                Msg.warn(client,"不可指定目标！");
                return;
            }
            GameObj target = null;
            try {
                int indexId = Integer.parseInt(msg);
                // 获取选择对象
                target = targetable.stream().filter(gameObj -> gameObj.id==indexId).findFirst().get();
            }catch (Exception e){
                Msg.warn(client,"指定目标错误！");
                return;
            }
            leader.skill(target);
            info.pushInfo();
        }
    }


    // TODO 测试用，顺序出牌

    public void test(Session client){

        String room = userRoom.get(client);
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
                Msg.warn(client,"场上放不下卡牌了！");
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
