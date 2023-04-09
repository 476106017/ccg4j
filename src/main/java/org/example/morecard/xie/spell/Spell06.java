package org.example.morecard.xie.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Damage;
import org.example.game.DamageMulti;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;


@Getter
@Setter
public class Spell06 extends SpellCard {
    public Integer cost = 1;
    public String name = "Spell06";
    public String job = "谢test";
    private List<String> race = Lists.ofStr();
    public String mark = """
        对场上全体随从造成1点伤害。
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()->{
            List<Damage> damages = new ArrayList<>();
            ownerPlayer().getAreaFollows().forEach(areaCard -> damages.add(new Damage(this,areaCard,1)));
            enemyPlayer().getAreaFollows().forEach(areaCard -> damages.add(new Damage(this,areaCard,1)));
            new DamageMulti(info,damages).apply();
        }));
    }

}
