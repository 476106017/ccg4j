package org.example.card.neutral.spell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.List;
import java.util.Random;

@EqualsAndHashCode(callSuper = true)
@Data
public class DarkSnare extends SpellCard {
    public Integer cost = 4;
    public String name = "暗黑陷阱";
    public String job = "中立";
    public String race = "陷阱";
    public String mark = """
        腐蚀：4 对随机一个敌方随从造成X点伤害,如果击杀：成长
        —————————————
        对一个敌方随从或对方玩家造成X点伤害
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

    @Override
    public Integer targetNum() {
        return 1;
    }

    @Override
    public List<GameObj> targetable() {
        List<GameObj> targetable = super.targetable();
        targetable.add(info.oppositePlayer().getLeader());
        targetable.addAll(info.oppositePlayer().getArea());
        return targetable;
    }

    @Override
    public void play(List<GameObj> targets) {
        super.play(targets);
        GameObj target = targets.get(0);
        Integer darkSnareDamage = getCount("damage");
        if(target instanceof FollowCard followCard){
            followCard.damaged(darkSnareDamage);
        } else if (target instanceof Leader leader) {
            info.damageLeader(leader,darkSnareDamage);
        }
    }

    @Override
    public Integer canRust() {
        return 4;
    }

    @Override
    public void afterRust() {
        info.msg(getNameWithOwner()+"触发腐蚀效果");
        List<AreaCard> follows = info.oppositePlayer().getArea()
            .stream().filter(areaCard -> "随从".equals(areaCard.getType()))
            .toList();
        if(!follows.isEmpty()){
            int randIndex = new Random().nextInt(follows.size());
            FollowCard followCard = (FollowCard) follows.get(randIndex);
            if(followCard.damaged(getCount("damage"))){
                count("damage");
            }
        }

    }
}
