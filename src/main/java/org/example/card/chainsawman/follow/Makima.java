package org.example.card.chainsawman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.card.chainsawman.equipment.DominatePipe;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Makima extends FollowCard {
    private int slot = 7;
    private int apposition = 1;
    private String name = "支配恶魔";
    private Integer cost = 0;
    private int atk = 1;
    private int hp = 1;
    private String job = "链锯人";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        入场时、回合开始时：为场上随机一名实名随从装备支配之线
        
        亡语：破坏一个装备支配之线的己方随从，将此卡召还到场上
        """;
    private String subMark = "";
    public Makima() {
        setMaxHp(getHp());
        getKeywords().add("恶魔转生");
        getEnterings().add(new AreaCard.Event.Entering(()->{
            List<FollowCard> canTarget = new ArrayList<>();
            canTarget.addAll(new ArrayList<>(enemyPlayer().getAreaFollows().stream()
                .filter(followCard -> followCard.getName().equals(followCard.prototype().getName())).toList()));
            canTarget.addAll(new ArrayList<>(ownerPlayer().getAreaFollows().stream()
                .filter(followCard -> followCard.getName().equals(followCard.prototype().getName())).toList()));

            if(canTarget.isEmpty())return;

            Lists.randOf(canTarget)
                .equip(createCard(DominatePipe.class));
        }));
        getEffectBegins().add(new AreaCard.Event.EffectBegin(()->{
            List<FollowCard> canTarget = new ArrayList<>();
            canTarget.addAll(new ArrayList<>(enemyPlayer().getAreaFollows().stream()
                .filter(followCard -> followCard.getName().equals(followCard.prototype().getName())).toList()));
            canTarget.addAll(new ArrayList<>(ownerPlayer().getAreaFollows().stream()
                .filter(followCard -> followCard.getName().equals(followCard.prototype().getName())).toList()));

            if(canTarget.isEmpty())return;

            Lists.randOf(canTarget)
                .equip(createCard(DominatePipe.class));
        }));
        getDeathRattles().add(new AreaCard.Event.DeathRattle(() -> {


        }));
    }
}
