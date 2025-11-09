package org.example.card.ccg.hunter.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.SpellCard;
import org.example.card.ccg.hunter.Rexxar;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class WildSpirits extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "野性之魂";
    public String job = "猎人";
    private List<String> race = Lists.ofStr();
    public String mark = """
        随机召唤2个灵种，场上灵种的倒数-1
        """;

    public String subMark = "";

    public void init() {
        setPlay(new Play(
            () -> {
                switch ((int)(Math.random()*3)){
                    case 0 -> ownerPlayer().summon(createCard(Rexxar.FoxSpiritWildseed.class));
                    case 1 -> ownerPlayer().summon(createCard(Rexxar.BearSpiritWildseed.class));
                    case 2 -> ownerPlayer().summon(createCard(Rexxar.StagSpiritWildseed.class));
                }
                switch ((int)(Math.random()*3)){
                    case 0 -> ownerPlayer().summon(createCard(Rexxar.FoxSpiritWildseed.class));
                    case 1 -> ownerPlayer().summon(createCard(Rexxar.BearSpiritWildseed.class));
                    case 2 -> ownerPlayer().summon(createCard(Rexxar.StagSpiritWildseed.class));
                }
                ownerPlayer().getAreaBy(areaCard ->
                    areaCard instanceof AmuletCard amuletCard && amuletCard.getRace().contains("灵种"))
                    .forEach(areaCard -> ((AmuletCard)areaCard).countDown());
            }));
    }
}
