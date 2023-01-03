package org.example.card.test.folow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;

@Getter
@Setter
public class TestFollow extends FollowCard {
    private String name = "测试随从";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 2;
    private String job = "测试";
    private List<String> race = Lists.ofStr();
    private String mark = """
        触发事件时发出信息
        """;
    private String subMark = "";


    public TestFollow() {
        setMaxHp(getHp());
        getKeywords().add("剧毒");
        setPlay(new Play(() -> getInfo().msg(getNameWithOwner() + "战吼")));
        getEnterings().add(new AreaCard.Event.Entering(()->
            getInfo().msg(getNameWithOwner() + "入场时")));
        getLeavings().add(new AreaCard.Event.Leaving(()->
            getInfo().msg(getNameWithOwner() + "离场时")));
        getDeathRattles().add(new AreaCard.Event.DeathRattle(()->
            getInfo().msg(getNameWithOwner() + "亡语")));
        getEffectBegins().add(new AreaCard.Event.EffectBegin(()->
            getInfo().msg(getNameWithOwner() + "回合开始时")));
        getEffectEnds().add(new AreaCard.Event.EffectEnd(()->
            getInfo().msg(getNameWithOwner() + "回合结束时")));
        getWhenAttacks().add(new Event.WhenAttack(damage->
            getInfo().msg(getNameWithOwner() + "攻击时")));
        getWhenBattles().add(new Event.WhenBattle(damage->
            getInfo().msg(getNameWithOwner() + "交战时")));
        getAfterDamageds().add(new Event.AfterDamaged(damage->
            getInfo().msg(getNameWithOwner() + "受伤时")));
        getWhenKills().add(new Card.Event.WhenKill(damage->
            getInfo().msg(getNameWithOwner() + "击杀时")));
        getExiles().add(new Card.Event.Exile(()->
            getInfo().msg(getNameWithOwner() + "除外时")));
        getTransmigrations().add(new Card.Event.Transmigration(()->
            getInfo().msg(getNameWithOwner() + "轮回时")));
        getWhenLeaderSkills().add(new Event.WhenLeaderSkill(()->
            getInfo().msg(getNameWithOwner() + "激励")));

    }
}