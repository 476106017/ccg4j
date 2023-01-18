package org.example.card.genshin;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.genshin.system.ElementBaseFollowCard;
import org.example.card.genshin.system.ElementCostSpellCard;
import org.example.card.genshin.system.Elemental;
import org.example.card.genshin.system.ElementalDamage;
import org.example.constant.EffectTiming;
import org.example.game.*;
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
        游戏开始时：搜索1张元素随从，获得1张普通攻击和1张切换角色
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
        除外指定手牌，生成1个与场上拥有【守护】的元素随从同元素骰子
        """;
    private int skillCost = 0;

    private List<Elemental> elementDices = new ArrayList<>();

    @Override
    public void init() {
        addEffect(new Effect(this,this, EffectTiming.BeginGame,()->{
            ownerPlayer().draw(card -> card instanceof ElementBaseFollowCard);
            ownerPlayer().addHand(createCard(NormalAttack.class));
            ownerPlayer().addHand(createCard(SwapCharacter.class));
        }), true);

        addEffect(new Effect(this,this, EffectTiming.BeginTurn,()->{
            List<AreaCard> follows = ownerPlayer().getAreaFollowsBy(followCard ->
                followCard instanceof ElementBaseFollowCard);
            if(follows.isEmpty() && info.getTurn()!=1)
                info.gameset(enemyPlayer());

            elementDices = new ArrayList<>();
            addDices(ownerPlayer().getPpNum());

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
        Card targetCard = (Card) target;

        // 将1张手牌除外
        info.exile(targetCard);

        // 生成1个与第1位角色同元素的骰子
        List<AreaCard> follows = ownerPlayer().getAreaFollowsBy(followCard ->
            followCard instanceof ElementBaseFollowCard);

        if(!follows.isEmpty()){
            ElementBaseFollowCard elementBaseFollowCard = (ElementBaseFollowCard) follows.get(0);
            info.msgToThisPlayer("生成了一个"+elementBaseFollowCard.getElement()+"骰子");
            getElementDices().add(elementBaseFollowCard.getElement());
        }

    }

    public int diceTypeNum(){
        return (int) elementDices.stream().filter(p->p!=Elemental.Universal).distinct().count();
    }

    public void addDices(int num){
        List<Elemental> dices = Arrays.stream(Elemental.values())
            .filter(Elemental::isDice).toList();
        for (int i = 0; i < num; i++) {
            elementDices.add(Lists.randOf(dices));
        }
    }

    public String showDices(){
        Map<Elemental, Long> count = elementDices.stream()
            .collect(Collectors.groupingBy(p->p, Collectors.counting()));
        StringBuilder sb = new StringBuilder("当前拥有的元素骰：\n");
        count.forEach((k,v)-> {
            sb.append("【").append(k.getStr().replaceAll("元素","")).append("】");
            if(v>1){
                sb.append("*").append(v).append("\t");
            }
            sb.append("\t");
        });
        sb.append("\n");
        return sb.toString();
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
    public void useDices(List<Elemental> costDices) {
        costDices.forEach(costDice -> {
            if (!elementDices.remove(costDice))
                elementDices.remove(Elemental.Universal);
        });
    }



    @Getter
    @Setter
    public static class NormalAttack extends ElementCostSpellCard {
        Integer cost = 2;
        public List<Elemental> elementCost = List.of(Elemental.Main, Elemental.Void, Elemental.Void);
        public String name = "普通攻击";
        public String job = "原神";
        private List<String> race = Lists.ofStr("行动");
        public String mark = """
        我方由第1位【守护】随从对指定目标发起攻击
        如果我方是法器随从，则造成对目标造成1+X点对应元素伤害
        否则对目标造成3+X点无元素伤害
        （X是我方随从攻击力）
        （效果伤害）
        """;
        public String subMark = "";

        public NormalAttack() {
            setPlay(new Play(
                ()->{
                    List<GameObj> enemyTargets = new ArrayList<>();
                    enemyTargets.add(info.oppositePlayer().getLeader());
                    enemyTargets.addAll(info.oppositePlayer().getAreaFollows(false));
                    return enemyTargets;
                }, true,
                toObj->{
                    ElementBaseFollowCard fromFollow = activeFollow();
                    fromFollow.count();
                    Damage damage;
                    if(fromFollow.hasRace("法器"))
                        damage = new ElementalDamage(fromFollow,toObj,
                            1 + fromFollow.getAtk(),fromFollow.getElement());
                    else if(fromFollow.getAttackElement() != Elemental.Void)
                        damage = new ElementalDamage(fromFollow,toObj,
                            3 + fromFollow.getAtk(),fromFollow.getAttackElement());
                    else
                        damage = new Damage(fromFollow,toObj,2 + fromFollow.getAtk());

                    fromFollow.getEffects(EffectTiming.WhenAttack)// 触发攻击时
                        .forEach(effect -> effect.getEffect().accept(damage));
                    damage.apply();
                }));
        }
    }

    @Getter
    @Setter
    public static class SwapCharacter extends ElementCostSpellCard {
        Integer cost = 1;
        public List<Elemental> elementCost = List.of(Elemental.Void);
        public String name = "切换角色";
        public String job = "原神";
        private List<String> race = Lists.ofStr("行动");
        public String mark = """
        指定一名我方场上的元素随从
        移除我方场上随从的【守护】效果，然后指定的随从获得【守护】
        """;
        public String subMark = "";

        public SwapCharacter() {
            setPlay(new Play(
                ()->ownerPlayer().getAreaFollowsAsGameObj(), true,
                toObj->{
                    ownerPlayer().getAreaFollows().forEach(areaCard -> areaCard.removeKeywordAll("守护"));
                    if(toObj instanceof FollowCard followCard)
                        followCard.addKeyword("守护");
                }));
        }
    }
}
