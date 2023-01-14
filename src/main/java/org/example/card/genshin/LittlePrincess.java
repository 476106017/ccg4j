package org.example.card.genshin;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.PlayerInfo;
import org.example.system.Lists;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Getter
@Setter
public class LittlePrincess extends Leader {
    private String name = "小王子";
    private String job = "原神";

    private String Mark = """
        游戏开始时：搜索3张元素随从，获得1张普通攻击和1张换人
        回合开始时：生成与PP点相同数量的随机元素骰；
        如果不是第一回合、且场上没有任何元素随从，则输掉游戏。
        """;

    // 超抽效果
    private String overDrawMark = """
        什么都不发生
        """;
    private Consumer<Integer> overDraw = integer -> {};

    private String skillName = "元素调和";
    private String skillMark =  """
        除外1张手牌，生成1个与场上拥有【守护】的元素随从同元素骰子
        """;
    private int skillCost = 0;

    private List<Elemental> elementDices = new ArrayList<>();

    @Override
    public void init() {
        addEffect(new Effect(this,this, EffectTiming.BeginGame,()->{
            ownerPlayer().draw(card -> card instanceof ElementBaseFollowCard,3);
            ownerPlayer().addHand(createCard(NormalAttack.class));
            ownerPlayer().addHand(createCard(NormalAttack.class));
        }), true);

        addEffect(new Effect(this,this, EffectTiming.BeginTurn,()->{
            List<AreaCard> follows = ownerPlayer().getAreaFollowsBy(followCard ->
                followCard instanceof ElementBaseFollowCard);
            if(follows.isEmpty() && info.getTurn()!=1)
                info.gameset(enemyPlayer());

            rollDices(ownerPlayer().getPpNum());
            showDices();

        }), true);
    }

    @Override
    public List<GameObj> targetable() {
        List<GameObj> targetable = super.targetable();
        targetable.addAll(ownerPlayer().getHand());
        return targetable;
    }

    @Override
    public void skill(GameObj target) {
        super.skill(target);
        PlayerInfo playerInfo = ownerPlayer();

        // 将1张手牌除外
        info.exile((Card) target);

        // 生成1个与第1位角色同元素的骰子
        List<AreaCard> follows = ownerPlayer().getAreaFollowsBy(followCard ->
            followCard instanceof ElementBaseFollowCard);

        if(!follows.isEmpty()){
            ElementBaseFollowCard elementBaseFollowCard = (ElementBaseFollowCard) follows.get(0);
            getElementDices().add(elementBaseFollowCard.getElement());
        }

    }

    public void rollDices(int num){
        List<Elemental> dices = Arrays.stream(Elemental.values())
            .filter(Elemental::isDice).toList();

        elementDices = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            elementDices.add(Lists.randOf(dices));
        }
    }

    public void showDices(){
        Map<Elemental, Long> count = elementDices.stream()
            .collect(Collectors.groupingBy(p->p, Collectors.counting()));
        StringBuilder sb = new StringBuilder("当前拥有的元素骰：\n");
        count.forEach((k,v)-> sb.append(k.getStr()).append("\t").append(v).append("\n"));
        info.msgTo(ownerPlayer().getUuid(),sb.toString());
    }
    public boolean hasDices(List<Elemental> costDices) {
        List<Elemental> diceRemains = new ArrayList<>(elementDices);
        try {
            costDices.forEach(costDice -> {
                if (!diceRemains.remove(costDice)
                    && !diceRemains.remove(Elemental.Universal))
                    throw new RuntimeException();
            });
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
