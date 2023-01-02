package org.example.constant;

import org.example.card.Card;
import org.example.card.chainsawman.follow.ChainsawMan;
import org.example.card.chainsawman.follow.DarkDemon;
import org.example.card.chainsawman.follow.Makima;
import org.example.card.deathnote.equipment.DeathNote;
import org.example.card.deathnote.follow.Lawliet;
import org.example.card.deathnote.follow.YagamiLight;
import org.example.card.fairy.amulet.FirespriteGrove;
import org.example.card.fairy.amulet.FlowerOfFairies;
import org.example.card.fairy.amulet.ForestSymphony;
import org.example.card.fairy.amulet.WoodOfBrambles;
import org.example.card.fairy.follow.*;
import org.example.card.fairy.spell.*;
import org.example.card.nemesis.follow.AnalyzingArtifact;
import org.example.card.nemesis.follow.ImmortalAegis;
import org.example.card.nemesis.spell.CalamitysGenesis;
import org.example.card.neutral.amulet.TestOfStrength;
import org.example.card.neutral.follow.*;
import org.example.card.neutral.spell.DarkSnare;
import org.example.card.neutral.spell.MysticRing;
import org.example.card.neutral.spell.PlanetaryFracture;
import org.example.card.rule.amulet.MahjongTable;
import org.example.card.stalker.follow.LabRecruiter;
import org.example.card.stalker.spell.spell.Preparation;
import org.example.card.stalker.spell.spell.Shadowstep;
import org.example.game.PlayerDeck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预置牌组
 */
public class DeckPreset {
    public static final Map<String,List<Class<? extends Card>>> decks = new HashMap<>();
    static {
        decks.put("默认",List.of(
            Bahamut.class, DarkSnare.class, TestOfStrength.class, AmbitiousGoblinMage.class,Hamsa.class,
            ColdlightOracle.class, MysticRing.class, TravelerGoblin.class, Zelgenea.class, PlanetaryFracture.class,

            MahjongTable.class, LabRecruiter.class, Preparation.class, Shadowstep.class, AnalyzingArtifact.class,
            ImmortalAegis.class, CalamitysGenesis.class, Lawliet.class, YagamiLight.class, DeathNote.class,

            ChainsawMan.class, DarkDemon.class, Makima.class, AirboundBarrage.class, ErosiveAnnihilation.class,
            Chronos.class,ForestGenesis.class, HeroicResolve.class,NaturesGuidance.class, PixieMischief.class,

            SylvanJustice.class,AncientElf.class,FairyWhisperer.class,InsectLord.class,PhantombloomPredator.class,
            QueenOfTheForest.class, FirespriteGrove.class, FlowerOfFairies.class, ForestSymphony.class, WoodOfBrambles.class
        ));
        decks.put("预设妖精",List.of(
            Bahamut.class, TestOfStrength.class, AmbitiousGoblinMage.class, AmbitiousGoblinMage.class,Hamsa.class,
            TravelerGoblin.class, TravelerGoblin.class, TravelerGoblin.class, Zelgenea.class, Zelgenea.class,

            MysticRing.class, PlanetaryFracture.class, FirespriteGrove.class, HeroicResolve.class,HeroicResolve.class,
            FlowerOfFairies.class, FlowerOfFairies.class, FlowerOfFairies.class, ForestSymphony.class, Chronos.class,

            WoodOfBrambles.class, WoodOfBrambles.class, PixieMischief.class, AncientElf.class, AncientElf.class,
            FairyWhisperer.class,FairyWhisperer.class, InsectLord.class,InsectLord.class, ErosiveAnnihilation.class,

            PhantombloomPredator.class,PhantombloomPredator.class,QueenOfTheForest.class,QueenOfTheForest.class,NaturesGuidance.class,
            AirboundBarrage.class, AirboundBarrage.class, AirboundBarrage.class, ForestGenesis.class, ForestGenesis.class
        ));
        decks.put("测试牌组",List.of(
            LabRecruiter.class, LabRecruiter.class, LabRecruiter.class, LabRecruiter.class
        ));
    }

    public static String describe(){
        StringBuilder sb = new StringBuilder();
        sb.append("预设牌组列表：\n");

        decks.forEach((k,v)->{
            sb.append("【" + k + "】\n");
//            sb.append(PlayerDeck.describeDeck(v));
            sb.append("\n");
        });
        sb.append("输入 usedeck <牌组名字> 使用预设牌组\n");

        return sb.toString();
    }
}
