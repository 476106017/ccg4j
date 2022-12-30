package org.example;

import org.example.card.Card;
import org.example.card.chainsawman.follow.ChainsawMan;
import org.example.card.chainsawman.follow.DarkDemon;
import org.example.card.chainsawman.follow.Makima;
import org.example.card.deathnote.equipment.DeathNote;
import org.example.card.deathnote.follow.YagamiLight;
import org.example.card.fairy.follow.FairyWhisperer;
import org.example.card.fairy.spell.NaturesGuidance;
import org.example.card.neutral.follow.TravelerGoblin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class Ccg4jApplication  {
    public static void main(String[] args) {
        SpringApplication.run(Ccg4jApplication.class, args);
    }

    // 测试牌组
    public static void editCards(List<Class<? extends Card>> activeDeck){
        for (int i = 0; i < 3; i++) {
            activeDeck.add(FairyWhisperer.class);
            activeDeck.add(NaturesGuidance.class);
            activeDeck.add(DeathNote.class);
            activeDeck.add(YagamiLight.class);
            activeDeck.add(Makima.class);
        }
    }
}
