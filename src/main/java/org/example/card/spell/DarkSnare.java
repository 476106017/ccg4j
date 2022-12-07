package org.example.card.spell;

import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.CardType;
import org.example.constant.Patten;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DarkSnare extends SpellCard {
    public static final String NAME = "暗黑陷阱";
    public static final String MARK = """
        吟唱：4点>=3 对随机一只敌方随从造成X点伤害,如果击杀：成长
        —————————————
        对一只敌方随从或对方玩家造成X点伤害
        """;

    public String subMark = "X等于{dark_snare_damage}";
    public String job = "陷阱";
    public int target = 1;
    {pattens.add(Patten.Fours);}

    @Override
    public void initCounter() {
        info.thisPlayer().count("dark_snare_damage");
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
        GameObj target = targets.get(0);
        Integer darkSnareDamage = info.thisPlayer().getCount("dark_snare_damage");
        if(target instanceof FollowCard followCard){
            if(followCard.damagedDeath(darkSnareDamage)){
                info.oppositePlayer().getArea().remove(followCard);
            }
        } else if (target instanceof Leader leader) {
            info.damageLeader(leader,darkSnareDamage);
        }
    }

    @Override
    public boolean canCantrip() {
        return Patten.Fours.getScore(info) >= 3;
    }

    @Override
    public void afterCantrip() {
        List<Card> follows = info.oppositePlayer().getArea()
            .stream().filter(card -> CardType.FOLLOW.equals(card.type))
            .toList();
        if(!follows.isEmpty()){
            int randIndex = new Random().nextInt(follows.size());
            FollowCard followCard = (FollowCard) follows.get(randIndex);
            Integer darkSnareDamage = info.thisPlayer().getCount("dark_snare_damage");
            if(followCard.damagedDeath(darkSnareDamage)){
                info.oppositePlayer().getArea().remove(followCard);
                info.thisPlayer().count("dark_snare_damage");
            }
        }

    }
}
