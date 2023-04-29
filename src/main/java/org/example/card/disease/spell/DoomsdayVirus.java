package org.example.card.disease.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class DoomsdayVirus extends SpellCard {
    public Integer cost = 4;
    public String name = "末日病毒";
    public String job = "疾病";
    private List<String> race = Lists.ofStr("灾厄");
    public String mark = """
        一种看不见的病毒正从对手牌堆开始传播
        回合结束时：病毒传染
        对方回合结束时：如果同一随从被感染多次，则获得-1/-1效果
        """;

    public String subMark = "";

    private Set<FollowCard> infected = new HashSet<>();
    private Set<FollowCard> infectedAgain = new HashSet<>();

    public void init() {
        setPlay(new Play(()->{
            final FollowCard deckRandomFollow = enemyPlayer().getDeckRandomFollow();
            if(deckRandomFollow==null){
                info.msg("牌堆没有任何宿主存在，病毒灭绝了！");
                return;
            }
            infected.add(deckRandomFollow);
            info.msg("一种看不见的病毒开始传播...");
            // region 回合结束传播
            ownerLeader().addEffect(new Effect(
                this,this, EffectTiming.EndTurn, () ->{

                final List<Card> deckInfected = enemyPlayer().getDeckBy(p -> infected.contains(p));
                for (int i = 0; i < deckInfected.size(); i++) {
                    final FollowCard randomFollow = enemyPlayer().getDeckRandomFollow();
                    if(infected.contains(randomFollow)) {
                        infectedAgain.add(randomFollow);
                    }
                    infected.add(randomFollow);
                }

                final List<Card> handInfected = enemyPlayer().getHandBy(p -> infected.contains(p));
                for (int i = 0; i < handInfected.size(); i++) {
                    final FollowCard randomFollow = enemyPlayer().getHandRandomFollow();
                    if(infected.contains(randomFollow)) {
                        infectedAgain.add(randomFollow);
                    }
                    infected.add(randomFollow);
                }

                final List<FollowCard> areaInfected = enemyPlayer().getAreaFollowsAsFollowBy(p -> infected.contains(p));
                for (int i = 0; i < areaInfected.size(); i++) {
                    final FollowCard randomFollow = (FollowCard) enemyPlayer().getAreaRandomFollow();
                    if(infected.contains(randomFollow)) {
                        infectedAgain.add(randomFollow);
                    }
                    infected.add(randomFollow);
                }
            }));
            // endregion 回合结束传播

            // region 敌方回合结束效果
            ownerLeader().addEffect(new Effect(
                this,this, EffectTiming.EnemyEndTurn, () ->{
                infectedAgain.forEach(followCard -> {
                    if(followCard.atArea() || followCard.atHand() || followCard.atDeck()){
                        followCard.addStatus(-1,-1);
                        if (followCard.getHp() <= 0 && !followCard.atArea()) {
                            followCard.removeWhenNotAtArea();
                            followCard.ownerPlayer().addGraveyard(followCard);
                        }
                    }
                });
            }));
            // endregion 敌方回合结束效果
        }));
    }

}
