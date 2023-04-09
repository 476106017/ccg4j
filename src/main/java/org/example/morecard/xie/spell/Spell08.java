package org.example.morecard.xie.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.SpellCard;
import org.example.game.Damage;
import org.example.game.DamageMulti;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class Spell08 extends SpellCard {
    public Integer cost = 4;
    public String name = "Spell08";
    public String job = "谢test";
    private List<String> race = Lists.ofStr();
    public String mark = """
        对敌方全体随从造成2点伤害。
        """;

    public String subMark = "";
    public void init() {
        setPlay(new Play(()->{
            final List<AreaCard> areaFollows = enemyPlayer().getAreaFollows();
            List<Damage> damages = new ArrayList<>();
            areaFollows.forEach(areaCard -> damages.add(new Damage(this,areaCard,2)));
            new DamageMulti(info,damages).apply();
        }));
    }

}
