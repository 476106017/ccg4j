package org.example.constant;

import org.example.card.Card;
import org.example.card.anime.chainsawman.follow.ChainsawMan;
import org.example.card.anime.chainsawman.follow.DarkDemon;
import org.example.card.anime.chainsawman.follow.Makima;
import org.example.card.anime.deathnote.equipment.DeathNote;
import org.example.card.anime.deathnote.follow.Lawliet;
import org.example.card.anime.deathnote.follow.YagamiLight;
import org.example.card.ccg.fairy.Alisa;
import org.example.card.dota.equipment.*;
import org.example.card.dota.follow.*;
import org.example.card.dota.spell.RoosterCrow;
import org.example.card.dota.spell.TeleportToBattleGround;
import org.example.card.ccg.fairy.amulet.FirespriteGrove;
import org.example.card.ccg.fairy.amulet.FlowerOfFairies;
import org.example.card.ccg.fairy.amulet.ForestSymphony;
import org.example.card.ccg.fairy.amulet.WoodOfBrambles;
import org.example.card.ccg.fairy.follow.*;
import org.example.card.ccg.fairy.spell.*;
import org.example.card.genshin.LittlePrincess;
import org.example.card.genshin.amulet.FakeSky;
import org.example.card.genshin.follow.Diluc;
import org.example.card.genshin.follow.Keaya;
import org.example.card.genshin.follow.Sucrose;
import org.example.card.genshin.spell.*;
import org.example.card.anime.jojo.follow.EnricoPucci;
import org.example.card.anime.jojo.follow.JolyneCujoh;
import org.example.card.anime.jojo.follow.KujoJotaro;
import org.example.card.ccg.nemesis.Yuwan;
import org.example.card.ccg.nemesis.follow.AnalyzingArtifact;
import org.example.card.ccg.nemesis.follow.ImmortalAegis;
import org.example.card.ccg.nemesis.spell.CalamitysGenesis;
import org.example.card.ccg.neutral.amulet.TestOfStrength;
import org.example.card.ccg.neutral.follow.*;
import org.example.card.ccg.neutral.spell.DarkSnare;
import org.example.card.ccg.neutral.spell.MysticRing;
import org.example.card.ccg.neutral.spell.PlanetaryFracture;
import org.example.card.other.rule.amulet.MahjongTable;
import org.example.card.ccg.rogue.follow.LabRecruiter;
import org.example.card.ccg.rogue.spell.Preparation;
import org.example.card.ccg.rogue.spell.Shadowstep;
import org.example.game.Leader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 预置牌组
 */
public class DeckPreset {
    public static final Map<String,List<Class<? extends Card>>> decks = new HashMap<>();
    public static final Map<String,Class<? extends Leader>> deckLeader = new HashMap<>();
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
        deckLeader.put("默认", Yuwan.class);
        decks.put("妖精",List.of(
            Bahamut.class, TestOfStrength.class, AmbitiousGoblinMage.class, AmbitiousGoblinMage.class,Hamsa.class,
            TravelerGoblin.class, TravelerGoblin.class, TravelerGoblin.class, Zelgenea.class, Zelgenea.class,

            MysticRing.class, PlanetaryFracture.class, FirespriteGrove.class, HeroicResolve.class,HeroicResolve.class,
            FlowerOfFairies.class, FlowerOfFairies.class, FlowerOfFairies.class, ForestSymphony.class, Chronos.class,

            WoodOfBrambles.class, WoodOfBrambles.class, PixieMischief.class, AncientElf.class, AncientElf.class,
            FairyWhisperer.class,FairyWhisperer.class, InsectLord.class,InsectLord.class, ErosiveAnnihilation.class,

            PhantombloomPredator.class,PhantombloomPredator.class,QueenOfTheForest.class,QueenOfTheForest.class,NaturesGuidance.class,
            AirboundBarrage.class, AirboundBarrage.class, AirboundBarrage.class, ForestGenesis.class, ForestGenesis.class
        ));
        deckLeader.put("妖精", Alisa.class);
        decks.put("dota",List.of(
            Roshan.class, Nevermore.class,Nevermore.class, FacelessVoid.class,FacelessVoid.class,
            DefenceTower.class,DefenceTower.class,DefenceTower.class,Slark.class,Slark.class,
            RoosterCrow.class, RoosterCrow.class, RoosterCrow.class,Vanguard.class, Vanguard.class,
            PoorMansSheild.class, PoorMansSheild.class, PoorMansSheild.class,DragonHeart.class, DragonHeart.class,
            TeleportToBattleGround.class, TeleportToBattleGround.class, TeleportToBattleGround.class, KingLeoric.class, KingLeoric.class,
            PhantomAssassin.class, PhantomAssassin.class,Alchemist.class,Alchemist.class, DivineRapier.class,
            MaskOfMadness.class, MaskOfMadness.class, ArcaneBoots.class,ArcaneBoots.class,ArcaneBoots.class,
            ShadowShaman.class,ShadowShaman.class,ShadowShaman.class,Zelgenea.class,Zelgenea.class
        ));
        decks.put("原神",List.of(
            Diluc.class, Keaya.class, Sucrose.class, FakeSky.class, ChaosMeteor.class,
            DawnOfWinery.class, ForgeSummon.class, Kokomi.class, Kokomi.class, LuckyDay.class
        ));
        deckLeader.put("原神", LittlePrincess.class);
        decks.put("jojo",List.of(
            EnricoPucci.class, EnricoPucci.class, EnricoPucci.class,
            JolyneCujoh.class,JolyneCujoh.class,JolyneCujoh.class,
            KujoJotaro.class,KujoJotaro.class,KujoJotaro.class
        ));
        decks.put("test",List.of(
            ShadowShaman.class,ShadowShaman.class,ShadowShaman.class,ShadowShaman.class,ShadowShaman.class,ShadowShaman.class,
            ShadowShaman.class,ShadowShaman.class,ShadowShaman.class,ShadowShaman.class,ShadowShaman.class,ShadowShaman.class
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
