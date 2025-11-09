package org.example.card.ccg.druid.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card._derivant.Derivant;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

import static org.example.constant.CounterKey.BLOCK;
import org.example.constant.CardRarity;

@Getter
@Setter
public class UltimateInfestation extends SpellCard {

   private CardRarity rarity = CardRarity.GOLD;
    public Integer cost = 10;
    public String name = "终极感染";
    public String job = "德鲁伊";
    private List<String> race = Lists.ofStr();
    public String mark = """
        造成5点伤害。抽5张牌。获得5点格挡。召唤一个5/5的僵尸。
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()->{
            final List<GameObj> targets = info.getAreaFollowsAsGameObj();
            targets.add(enemyLeader());
            targets.add(ownerLeader());
            return targets;
        },
        true,target->{
            info.damageEffect(this,target,5 );
            ownerPlayer().draw(5);
            ownerPlayer().count(BLOCK,5);
            ownerPlayer().summon(createCard(Derivant.Zombie.class,5,5));
        }));
    }

}
