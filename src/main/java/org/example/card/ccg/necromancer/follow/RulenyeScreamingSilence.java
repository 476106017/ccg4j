package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.DEATH_PREFIX;

@Getter
@Setter
public class RulenyeScreamingSilence extends FollowCard {
    private String name = "绝叫沉默·鲁鲁纳伊";
    private Integer cost = 3;
    private int atk = 2;
    private int hp = 1;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：+X/+0，如果X大于10，则对敌方主战者造成3点伤害，并且使这个随从和随机1名敌方随从除外
        亡语：获得1张【绝叫的扩散】
        （X是【绝叫沉默·鲁鲁纳伊】的死亡数量）
        """;
    public String subMark = "X等于{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}", ownerPlayer().getCount(DEATH_PREFIX+getName())+"");
    }
    public void init() {
        setMaxHp(getHp());
        getKeywords().add("突进");
        setPlay(new Play(
            () -> {
                int x = ownerPlayer().getCount(DEATH_PREFIX+getName());
                addStatus(x,0);
                if(x>=10){
                    info.exile(enemyPlayer().getAreaRandomFollow());
                    info.damageEffect(this,enemyLeader(),3);
                    info.exile(this);
                }
            }));
        addEffects((new Effect(this,this,
            EffectTiming.DeathRattle, obj -> {
            ownerPlayer().addHand(createCard(ScreamDiffusion.class));
        })));
    }

    @Getter
    @Setter
    public static class ScreamDiffusion extends SpellCard {
        public Integer cost = 1;
        public String name = "绝叫的扩散";
        public String job = "死灵术士";
        private List<String> race = Lists.ofStr();
        public String mark = """
        创造X个【绝叫沉默·鲁鲁纳伊】，使他们的费用变成1并移除亡语，召唤他们
        死灵术 3：获得1张【绝叫沉默·鲁鲁纳伊】
        （X是【绝叫沉默·鲁鲁纳伊】的死亡数量）
        """;

        public String subMark = "X等于{}";

        public String getSubMark() {
            return subMark.replaceAll("\\{}", ownerPlayer().getCount(DEATH_PREFIX+getName())+"");
        }


        public void init() {
            setPlay(new Play(
                ()->{
                    int x = ownerPlayer().getCount(DEATH_PREFIX+getName());
                    List<AreaCard> list = new ArrayList<>();
                    for (int i = 0; i < x; i++) {
                        RulenyeScreamingSilence screamingSilence = createCard(RulenyeScreamingSilence.class);
                        screamingSilence.setCost(1);
                        List<Effect> deathRattle =
                            screamingSilence.getEffects(EffectTiming.DeathRattle);
                        screamingSilence.getEffects().removeAll(deathRattle);
                        list.add(screamingSilence);
                    }
                    ownerPlayer().summon(list);

                    ownerPlayer().costGraveyardCountTo(3,()->{
                        ownerPlayer().addHand(createCard(RulenyeScreamingSilence.class));
                    });
                }));
        }

    }

}