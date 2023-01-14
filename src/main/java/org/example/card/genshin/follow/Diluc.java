package org.example.card.genshin.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.genshin.ElementBaseFollowCard;
import org.example.card.genshin.ElementCostSpellCard;
import org.example.card.genshin.Elemental;
import org.example.game.Damage;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Diluc extends ElementBaseFollowCard {
    private String name = "迪卢克";
    private int atk = 0;
    private int hp = 10;
    private String job = "原神";
    private List<String> race = Lists.ofStr("元素","双手剑");

    private Elemental element = Elemental.Pydro;

    private String mark = """
        战吼：获得1张逆焰之刃
        元素充能（3）：获得1张黎明
        """;
    private String subMark = "充能进度：{}/3";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    public Diluc() {
        setMaxHp(getHp());
        getKeywords().add("缴械");
        getKeywords().add("无法破坏");
        setPlay(new Play(
            ()->ownerPlayer().addHand(createCard(SearingOnslaught.class))
        ));
    }

    @Override
    public void elementalBurst() {
        ownerPlayer().addHand(createCard(SearingOnslaught.class));
    }

    @Getter
    @Setter
    public static class SearingOnslaught extends ElementCostSpellCard {
        public List<Elemental> elementCost = List.of(Elemental.Main, Elemental.Main, Elemental.Main);
        public String name = "逆焰之刃";
        public String job = "原神";
        private List<String> race = Lists.ofStr("技能","元素战技");
        public String mark = """
        对敌方随从造成3+X点火元素伤害（X是迪卢克攻击力）
        """;
        public String subMark = "X等于{}";
        public String getSubMark() {
            return subMark.replaceAll("\\{}",((FollowCard)getParent()).getAtk()+"");
        }

        public SearingOnslaught() {
            setPlay(new Play(
                ()->{
                    List<GameObj> enemyTargets = new ArrayList<>();
                    enemyTargets.add(info.oppositePlayer().getLeader());
                    boolean ignoreGuard = hasKeyword("无视守护");
                    enemyTargets.addAll(info.oppositePlayer().getAreaFollows(ignoreGuard));
                    return enemyTargets;
                },
               true,
                obj->{
                    FollowCard fromFollow = (FollowCard)getParent();
                    new Damage(fromFollow,obj,3 + fromFollow.getAtk());
                    fromFollow.count();
                }));
        }
    }
}