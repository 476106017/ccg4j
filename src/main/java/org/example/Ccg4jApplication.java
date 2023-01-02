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
}
