package org.example.card.ccg.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.Damage;
import org.example.game.DamageMulti;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;


@Getter
@Setter
public class AriasWhirlwind extends SpellCard {
    public Integer cost = 2;
    public String name = "阿丽雅的旋风";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        给予双方的从者全体（妖精族随从除外）X点伤害。 X为本回合中已使用的卡片张数（不包含本卡片）
        """;

    public String subMark = "X等于{}";
    public String getSubMark() {
        return subMark.replaceAll("\\{}",ownerPlayer().getCount(PLAY_NUM)+"");
    }

    public void init() {
        setPlay(new Play(()->{
            List<Damage> damages = new ArrayList<>();
            Integer count = ownerPlayer().getCount(PLAY_NUM);
            ownerPlayer().getAreaFollows().stream()
                .filter(areaCard -> !areaCard.hasRace("妖精"))
                .forEach(areaCard -> damages.add(new Damage(this,areaCard,count)));
            enemyPlayer().getAreaFollows().stream()
                .filter(areaCard -> !areaCard.hasRace("妖精"))
                .forEach(areaCard -> damages.add(new Damage(this,areaCard,count)));
            new DamageMulti(info,damages).apply();
        }));
    }
}
