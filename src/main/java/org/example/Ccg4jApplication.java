package org.example;

import org.example.card.Card;
import org.example.card.chainsawman.follow.ChainsawMan;
import org.example.card.chainsawman.follow.DarkDemon;
import org.example.card.chainsawman.follow.Makima;
import org.example.card.deathnote.equipment.DeathNote;
import org.example.card.deathnote.follow.Lawliet;
import org.example.card.deathnote.follow.YagamiLight;
import org.example.card.fairy.amulet.ForestSymphony;
import org.example.card.fairy.follow.FairyWhisperer;
import org.example.card.fairy.spell.ForestGenesis;
import org.example.card.fairy.spell.NaturesGuidance;
import org.example.card.nemesis.follow.AnalyzingArtifact;
import org.example.card.nemesis.follow.ImmortalAegis;
import org.example.card.nemesis.spell.CalamitysGenesis;
import org.example.card.neutral.follow.AmbitiousGoblinMage;
import org.example.card.neutral.follow.Bahamut;
import org.example.card.neutral.follow.ColdlightOracle;
import org.example.card.neutral.follow.TravelerGoblin;
import org.example.card.neutral.spell.DarkSnare;
import org.example.card.rule.amulet.MahjongTable;
import org.example.card.stalker.follow.LabRecruiter;
import org.example.system.Lists;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class Ccg4jApplication  {
    public static void main(String[] args) {
        SpringApplication.run(Ccg4jApplication.class, args);
    }

    // 测试牌组
    public static void editCards(List<Class<? extends Card>> activeDeck){
        List<Class<? extends Card>> addCards = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            addCards.add(Bahamut.class);
            addCards.add(DarkSnare.class);
            addCards.add(TravelerGoblin.class);
            addCards.add(ColdlightOracle.class);
            addCards.add(AmbitiousGoblinMage.class);

            addCards.add(ForestSymphony.class);
            addCards.add(ForestGenesis.class);
            addCards.add(FairyWhisperer.class);
            addCards.add(NaturesGuidance.class);

            addCards.add(CalamitysGenesis.class);
            addCards.add(AnalyzingArtifact.class);
            addCards.add(ImmortalAegis.class);

            addCards.add(ChainsawMan.class);
            addCards.add(DarkDemon.class);
            addCards.add(Makima.class);

            addCards.add(DeathNote.class);
            addCards.add(YagamiLight.class);
            addCards.add(Lawliet.class);

            addCards.add(LabRecruiter.class);

            addCards.add(MahjongTable.class);
        }
        activeDeck.addAll(addCards);
    }
}
