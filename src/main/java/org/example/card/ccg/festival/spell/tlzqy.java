package org.example.card.ccg.festival.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.card._derivant.Derivant;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.NECROMANCY_NUM;
import org.example.constant.CardRarity;


@Getter
@Setter
public class tlzqy extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 10;
    public String name = "通灵最强音";
    public String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    public String mark = """
        造成5点伤害，召唤2个X/X的僵尸（X是你本局比赛消耗墓地数总和）
        """;

    public String subMark = "X等于{}";
    public String getSubMark() {
        return subMark.replaceAll("\\{}",ownerPlayer().getCount(NECROMANCY_NUM)+"");
    }

    public void init() {
        getKeywords().add("吸血");
        setPlay(new Play(()->{
            List<GameObj> targetable = new ArrayList<>();
            targetable.add(info.oppositePlayer().getLeader());
            targetable.addAll(info.oppositePlayer().getAreaFollows());
            return targetable;
        },
            true,
            target->{
                info.damageEffect(this,target,5);

                Integer x = ownerPlayer().getCount(NECROMANCY_NUM);
                ownerPlayer().summon(List.of(
                    createCard(Derivant.Zombie.class,x,x),
                    createCard(Derivant.Zombie.class)));
            }
        ));
    }
}
