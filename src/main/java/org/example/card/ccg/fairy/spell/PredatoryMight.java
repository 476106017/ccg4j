package org.example.card.ccg.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.Comparator;
import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class PredatoryMight extends SpellCard {

   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 2;
    public String name = "强者的威严";
    public String job = "妖精";
    private List<String> race = Lists.ofStr();
    public String mark = """
        给予1个攻击力最低的敌方从者5点伤害。
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(()->{
            enemyPlayer().getAreaFollowsAsFollow().stream().min(Comparator.comparingInt(FollowCard::getAtk))
                .ifPresent(followCard -> {
                info.damageEffect(this,followCard,5);
            });
        }));
    }
}
