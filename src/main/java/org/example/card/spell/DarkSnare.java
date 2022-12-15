package org.example.card.spell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.CardType;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.List;
import java.util.Random;

@EqualsAndHashCode(callSuper = true)
@Data
public class DarkSnare extends SpellCard {
    public Integer cost = 4;
    public String name = "暗黑陷阱";
    public String job = "陷阱";
    public String mark = """
        腐蚀：4 对随机一只敌方随从造成X点伤害,如果击杀：成长
        —————————————
        对一只敌方随从或对方玩家造成X点伤害
        """;

    public String subMark = "X等于{dark_snare_damage}";
    public int target = 1;

    public String getSubMark() {
        return subMark.replaceAll("\\{dark_snare_damage}",
            info.getPlayerInfos()[owner].getCount("dark_snare_damage")+"");
    }

    @Override
    public void initCounter() {
        // 第一次加载这张牌时候才初始化计数器
        if(info.getPlayerInfos()[owner].getCount("dark_snare_damage")==null){
            info.getPlayerInfos()[owner].count("dark_snare_damage");
        }
    }

    @Override
    public List<GameObj> targetable() {
        List<GameObj> targetable = super.targetable();
        targetable.addAll(info.oppositePlayer().getArea());
        targetable.add(info.oppositePlayer().getLeader());
        return targetable;
    }

    @Override
    public void play(List<GameObj> targets) {
        super.play(targets);
        GameObj target = targets.get(0);
        Integer darkSnareDamage = info.getPlayerInfos()[owner].getCount("dark_snare_damage");
        if(target instanceof FollowCard followCard){
            if(followCard.damagedDeath(darkSnareDamage)){
                info.oppositePlayer().getArea().remove(followCard);
            }
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
        List<Card> follows = info.oppositePlayer().getArea()
            .stream().filter(card -> CardType.FOLLOW.equals(card.getType()))
            .toList();
        if(!follows.isEmpty()){
            int randIndex = new Random().nextInt(follows.size());
            FollowCard followCard = (FollowCard) follows.get(randIndex);
            Integer darkSnareDamage = info.getPlayerInfos()[owner].getCount("dark_snare_damage");
            if(followCard.damagedDeath(darkSnareDamage)){
                info.oppositePlayer().getArea().remove(followCard);
                info.getPlayerInfos()[owner].count("dark_snare_damage");
            }
        }

    }
}
