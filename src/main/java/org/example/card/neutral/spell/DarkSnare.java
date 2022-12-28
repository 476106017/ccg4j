package org.example.card.neutral.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Damage;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Getter
@Setter
public class DarkSnare extends SpellCard {
    public Integer cost = 4;
    public String name = "暗黑陷阱";
    public String job = "中立";
    private List<String> race = Lists.ofStr("陷阱");
    public String mark = """
        腐蚀：4 对随机1个敌方随从造成X点伤害
        击杀时：X+1
        —————————————
        对1个敌方随从或对方玩家造成X点伤害
        """;

    public String subMark = "X等于{damage}";
    public int target = 1;

    public String getSubMark() {
        return subMark.replaceAll("\\{damage}",getCount("damage")+"");
    }

    @Override
    public void initCounter() {
        this.count("damage");
    }

    public DarkSnare() {
        getPlays().add(new Card.Event.Play(()->{
                List<GameObj> targetable = new ArrayList<>();
                targetable.add(info.oppositePlayer().getLeader());
                targetable.addAll(info.oppositePlayer().getAreaFollows());
                return targetable;
            },
            1,
            targets->{
                GameObj target = targets.get(0);
                Integer darkSnareDamage = getCount("damage");
                Damage damage = new Damage(this, target, darkSnareDamage);
                if(target instanceof FollowCard followCard){
                    followCard.damaged(damage);
                } else if (target instanceof Leader leader) {
                    leader.damaged(damage);
                }
            }
        ));
        getBoosts().add(new Event.Boost(
            card-> card.getCost()>=5,
            card->{
                List<AreaCard> follows = info.oppositePlayer().getArea()
                    .stream().filter(areaCard -> "随从".equals(areaCard.getType()))
                    .toList();
                if(!follows.isEmpty()){
                    int randIndex = new Random().nextInt(follows.size());
                    FollowCard followCard = (FollowCard) follows.get(randIndex);
                    followCard.damaged(new Damage(this,followCard,getCount("damage")));
                }
            }
        ));
        getWhenKills().add(new Event.WhenKill(followCard -> {
            count("damage");
        }));
    }

}
