package org.example.card.ccg.vampire.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class RazoryClaw extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 2;
    public String name = "利爪的一击";
    public String job = "吸血鬼";
    private List<String> race = Lists.ofStr();
    public String mark = """
        给予自己的主战者2点伤害，
        并给予敌方的主战者或1个敌方的从者3点伤害。
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            List<GameObj> targetable = new ArrayList<>();
            targetable.add(info.oppositePlayer().getLeader());
            targetable.addAll(info.oppositePlayer().getAreaFollows());
            return targetable;
        },
            true,
            target->{
                info.damageEffect(this,ownerLeader(),2);
                if(target instanceof FollowCard followCard){
                    info.damageEffect(this,followCard,3);
                } else if (target instanceof Leader leader) {
                    leader.damaged(this,3);
                }
            }));
    }

}
