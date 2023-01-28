package org.example.card.ccg.fairy;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.ccg.fairy.follow.Fairy;
import org.example.constant.CounterKey;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.PlayerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


@Getter
@Setter
public class Alisa extends Leader {
    private String name = "亚里莎";
    private String job = "妖精";

    private String skillName = "妖精的友谊";
    private String skillMark =  """
        回合使用数+1，并从以下能力中随机发动1种
        1. 将1张妖精召唤到战场
        2. 将2张妖精加入手牌
        3. 将3张妖精放入墓地
        """;
    private int skillCost = 2;

    private boolean needTarget = false;

    private String overDrawMark =  """
        从墓地里抽1张妖精并获得【游魂】，若墓地没有妖精，则输掉游戏
        """;

    private Consumer<Integer> overDraw = integer -> {

        PlayerInfo player = ownerPlayer();
        Optional<Card> any = player.getGraveyard().stream().filter(card -> card instanceof Fairy).findAny();
        if(any.isPresent()){
            Card fairy = any.get();
            fairy.addKeyword("游魂");
            player.getGraveyard().remove(fairy);
            player.addHand(fairy);
        }else
            info.gameset(enemyPlayer());
    };


    @Override
    public void skill(GameObj target) {
        super.skill(target);
        PlayerInfo playerInfo = ownerPlayer();

        playerInfo.count(CounterKey.PLAY_NUM);

        if(Math.random()*3 < 1){
            playerInfo.summon(createCard(Fairy.class));
            return;
        }
        if(Math.random()*3 < 2){
            List<Card> addList = new ArrayList<>();
            addList.add(createCard(Fairy.class));
            addList.add(createCard(Fairy.class));
            playerInfo.addHand(addList);
            return;
        }
        List<Card> addList = new ArrayList<>();
        addList.add(createCard(Fairy.class));
        addList.add(createCard(Fairy.class));
        addList.add(createCard(Fairy.class));
        playerInfo.addGraveyard(addList);
    }
}
