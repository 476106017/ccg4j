package org.example.card.chainsawman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
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
        战吼、回合开始时：找到场上随机一名未装备支配之线的实名随从，装备支配之线
        
        亡语：破坏一个装备支配之线的己方随从，将此卡召还到场上
        """;
    private String subMark = "";
    public Makima() {
        setMaxHp(getHp());
        getKeywords().add("恶魔转生");
        getPlays().add(new Card.Event.Play(() -> {
            List<FollowCard> canTarget = new ArrayList<>();
            canTarget.addAll(new ArrayList<>(enemyPlayer().getAreaFollowsAsFollow().stream()
                .filter(followCard -> followCard.isRealName()
                    && !(followCard.getName().equals("支配恶魔"))
                    && !(followCard.equipmentNamed("支配之线")))
                .toList()));
            canTarget.addAll(new ArrayList<>(ownerPlayer().getAreaFollowsAsFollow().stream()
                .filter(followCard -> followCard.isRealName()
                    && !(followCard.getName().equals("支配恶魔"))
                    && !(followCard.equipmentNamed("支配之线")))
                .toList()));

            if(canTarget.isEmpty())return;

            Lists.randOf(canTarget)
                .equip(createCard(DominatePipe.class));
        }));
        getEffectBegins().add(new AreaCard.Event.EffectBegin(()->{
            List<FollowCard> canTarget = new ArrayList<>();
            canTarget.addAll(new ArrayList<>(enemyPlayer().getAreaFollowsAsFollow().stream()
                .filter(followCard -> followCard.isRealName()
                    && !(followCard.getName().equals("支配恶魔"))
                    && !(followCard.equipmentNamed("支配之线")))
                .toList()));
            canTarget.addAll(new ArrayList<>(ownerPlayer().getAreaFollowsAsFollow().stream()
                .filter(followCard -> followCard.isRealName()
                    && !(followCard.getName().equals("支配恶魔"))
                    && !(followCard.equipmentNamed("支配之线")))
                .toList()));
            if(canTarget.isEmpty())return;

            Lists.randOf(canTarget)
                .equip(createCard(DominatePipe.class));
        }));
        getDeathRattles().add(new AreaCard.Event.DeathRattle(() -> {

            ownerPlayer().getAreaCopy().stream()
                .filter(areaCard -> areaCard instanceof FollowCard follow
                    && follow.equipmentNamed("支配之线"))
                .findAny().ifPresent(areaCard -> {
                    areaCard.destroyedBy(this);
                    ownerPlayer().recall(this);
                });

        }));
    }
}
