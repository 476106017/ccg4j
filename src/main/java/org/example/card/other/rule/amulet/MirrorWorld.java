package org.example.card.other.rule.amulet;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class MirrorWorld extends AmuletCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "镜中世界";
    public String job = "游戏规则";
    private List<String> race = Lists.ofStr();
    public String mark = """
        敌方出牌时：使我方手牌上同名卡牌消费变为0
        """;

    public String subMark = "";

    public void init() {
        addEffects((new Effect(this,this, EffectTiming.WhenEnemyPlay, obj->{
            Card playCard = (Card) obj;
            ownerPlayer().getHandBy(card -> card.getName().equals(playCard.getName()))
                .forEach(card->card.setCost(0));
        })));
    }
}
