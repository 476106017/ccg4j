package org.example.card.chainsawman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.chainsawman.equipment.DominatePipe;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Makima extends FollowCard {
    private int slot = 7;
    private int apposition = 1;
    private String name = "支配恶魔";
    private Integer cost = 5;
    private int atk = 1;
    private int hp = 1;
    private String job = "链锯人";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        战吼/回合开始时：找到场上随机一名未装备支配之线的实名随从，装备支配之线
        
        亡语：破坏一个装备支配之线的我方随从，将此卡召还到场上
        """;
    private String subMark = "";
    public Makima() {
        setMaxHp(getHp());
        getKeywords().add("恶魔转生");
        setPlay(new Play(() -> {
            List<FollowCard> canTarget = new ArrayList<>();
            canTarget.addAll(new ArrayList<>(enemyPlayer().getAreaFollowsAsFollow().stream()
                .filter(followCard ->!(followCard.getName().equals("支配恶魔"))
                    && !(followCard.equipmentNamed("支配之线")))
                .toList()));
            canTarget.addAll(new ArrayList<>(ownerPlayer().getAreaFollowsAsFollow().stream()
                .filter(followCard ->!(followCard.getName().equals("支配恶魔"))
                    && !(followCard.equipmentNamed("支配之线")))
                .toList()));

            if(canTarget.isEmpty())return;

            FollowCard target = Lists.randOf(canTarget);
            if(target.isRealName()){
                equip(createCard(DominatePipe.class));
            }else {
                info.msg(getNameWithOwner()+"发现自己无法支配"+target.getName());
            }

        }));
        addEffects((new Effect(this,this, EffectTiming.BeginTurn, obj->{
            List<FollowCard> canTarget = new ArrayList<>();
            canTarget.addAll(new ArrayList<>(enemyPlayer().getAreaFollowsAsFollow().stream()
                .filter(followCard -> !(followCard.getName().equals("支配恶魔"))
                    && !(followCard.equipmentNamed("支配之线")))
                .toList()));
            canTarget.addAll(new ArrayList<>(ownerPlayer().getAreaFollowsAsFollow().stream()
                .filter(followCard -> !(followCard.getName().equals("支配恶魔"))
                    && !(followCard.equipmentNamed("支配之线")))
                .toList()));
            if(canTarget.isEmpty())return;

            FollowCard target = Lists.randOf(canTarget);
            if(target.isRealName()){
                equip(createCard(DominatePipe.class));
            }else {
                info.msg(getNameWithOwner()+"发现自己无法支配"+target.getName());
            }
        })));


        addEffects((new Effect(this,this, EffectTiming.DeathRattle, obj -> {

            ownerPlayer().getAreaCopy().stream()
                .filter(areaCard -> areaCard instanceof FollowCard follow
                    && follow.equipmentNamed("支配之线"))
                .findAny().ifPresent(areaCard -> {
                    areaCard.destroyedBy(this);
                    ownerPlayer().recall(this);
                });

        })));
    }
}
