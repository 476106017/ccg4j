package org.example.card.genshin;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.game.Damage;
import org.example.game.ElementalDamage;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class NormalAttack extends ElementCostSpellCard {
    public Integer cost = 10;
    public List<Elemental> elementCost = List.of(Elemental.Main, Elemental.Void, Elemental.Void);
    public String name = "普通攻击";
    public String job = "原神";
    private List<String> race = Lists.ofStr("技能");
    public String mark = """
    我方由第1位【守护】随从对指定目标发起攻击
    如果我方是法器随从，则造成对目标造成1+X点对应元素伤害
    否则对目标造成3+X点无元素伤害
    （X是我方随从攻击力）
    （效果伤害）
    """;
    public String subMark = "";

    public NormalAttack() {
        getKeywords().add("无限");
        setPlay(new Play(
            ()->{
                List<GameObj> enemyTargets = new ArrayList<>();
                enemyTargets.add(info.oppositePlayer().getLeader());
                enemyTargets.addAll(info.oppositePlayer().getAreaFollows(false));
                return enemyTargets;
            }, true,
            toObj->{
                List<AreaCard> guards = ownerPlayer().getAreaFollowsBy(followCard ->
                    followCard instanceof ElementBaseFollowCard elementBaseFollowCard && elementBaseFollowCard.hasKeyword("守护"));
                if(guards.isEmpty()){
                    info.msg("没有可以攻击的随从，技能没有任何效果！");
                    return;
                }
                ElementBaseFollowCard fromFollow = (ElementBaseFollowCard) guards.get(0);
                fromFollow.count();
                if(fromFollow.hasRace("法器"))
                    new ElementalDamage(fromFollow,toObj,
                        1 + fromFollow.getAtk(),fromFollow.getElement()).apply();
                else
                    new Damage(fromFollow,toObj,3 + fromFollow.getAtk()).apply();
            }));
    }
}