package org.example.morecard.genshin.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;
import org.example.morecard.genshin.LittlePrincess;
import org.example.morecard.genshin.system.ElementBaseFollowCard;
import org.example.morecard.genshin.system.Elemental;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Leader;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class FakeSky extends AmuletCard {

    public Integer cost = 3;

    public String name = "虚假的星空";
    public String job = "原神";
    private List<String> race = Lists.ofStr();
    public int countDown = 3;

    public String mark = """
        回合开始时：+X个万能元素骰子（X是己方场上元素随从个数）
        亡语：召唤黑之契约者
        """;
    public String subMark = "";

    public FakeSky() {
        addEffects((new Effect(this,this, EffectTiming.BeginTurn, ()->{
            long count = ownerPlayer()
                .getAreaFollowsBy(followCard -> followCard instanceof ElementBaseFollowCard).size();

            Leader leader = ownerLeader();
            if(leader instanceof LittlePrincess littlePrincess){
                for (int i = 0; i < count; i++) {
                    littlePrincess.getElementDices().add(Elemental.Universal);
                }
            }
        })));

        addEffects((new Effect(this,this, EffectTiming.DeathRattle, ()->{
            long count = ownerPlayer()
                .getAreaFollowsBy(followCard -> followCard instanceof ElementBaseFollowCard).size();

            Leader leader = ownerLeader();
            if(leader instanceof LittlePrincess littlePrincess){
                for (int i = 0; i < count; i++) {
                    littlePrincess.getElementDices().add(Elemental.Universal);
                }
            }
        })));

    }

    @Getter
    @Setter
    public static class Hei extends FollowCard {
        private String name = "黑之契约者";
        private Integer cost = 3;
        private int atk = 1;
        private int hp = 1;
        private String job = "黑之契约者";
        private List<String> race = Lists.ofStr();
        private String mark = """
        亡语：我方下个回合开始时，除外双方场上全部元素随从，将元素骰全部变为PP
        """;
        private String subMark = "";

        public Hei() {
            setMaxHp(getHp());
            getKeywords().add("守护");
            getKeywords().add("剧毒");

            addEffects((new Effect(this,this, EffectTiming.DeathRattle, ()->{
                info.msg("【总有一天我要撕碎这虚假的星空。】");

                Leader leader = ownerLeader();
                leader.addEffect(new Effect(this,leader,EffectTiming.BeginTurn,()->{
                    info.exile(ownerPlayer().getAreaFollowsAsCardBy(p->p instanceof ElementBaseFollowCard));
                    info.exile(enemyPlayer().getAreaFollowsAsCardBy(p->p instanceof ElementBaseFollowCard));

                    if(leader instanceof LittlePrincess littlePrincess){
                        List<Elemental> dices = littlePrincess.getElementDices();
                        ownerPlayer().setPpNum(ownerPlayer().getPpNum() + dices.size());
                        dices.clear();
                    }
                }),true);
            })));

        }
    }
}
