package org.example.constant;

import org.example.card.Card;
import org.example.card._derivant.Derivant;
import org.example.card.anime.chainsawman.follow.ChainsawMan;
import org.example.card.anime.chainsawman.follow.DarkDemon;
import org.example.card.anime.chainsawman.follow.Makima;
import org.example.card.anime.deathnote.equipment.DeathNote;
import org.example.card.anime.deathnote.follow.Lawliet;
import org.example.card.anime.deathnote.follow.YagamiLight;
import org.example.card.anime.jojo.follow.EnricoPucci;
import org.example.card.anime.jojo.follow.JolyneCujoh;
import org.example.card.anime.jojo.follow.KujoJotaro;
import org.example.card.ccg.fairy.Alisa;
import org.example.card.ccg.fairy.amulet.*;
import org.example.card.ccg.fairy.follow.*;
import org.example.card.ccg.fairy.spell.*;
import org.example.card.ccg.festival.follow.*;
import org.example.card.ccg.festival.spell.hsyg;
import org.example.card.ccg.festival.spell.tbcz;
import org.example.card.ccg.festival.spell.yejs;
import org.example.card.ccg.festival.spell.zxsh;
import org.example.card.ccg.hunter.Rexxar;
import org.example.card.ccg.hunter.equipment.HarpoonGun;
import org.example.card.ccg.hunter.follow.*;
import org.example.card.ccg.hunter.spell.*;
import org.example.card.ccg.necromancer.Luna;
import org.example.card.ccg.necromancer.amulet.*;
import org.example.card.ccg.necromancer.follow.*;
import org.example.card.ccg.necromancer.spell.*;
import org.example.card.ccg.nemesis.Yuwan;
import org.example.card.ccg.nemesis.follow.ImmortalAegis;
import org.example.card.ccg.nemesis.spell.CalamitysGenesis;
import org.example.card.ccg.neutral.ThePlayer;
import org.example.card.ccg.neutral.amulet.TestOfStrength;
import org.example.card.ccg.neutral.follow.*;
import org.example.card.ccg.neutral.spell.*;
import org.example.card.ccg.rogue.follow.LabRecruiter;
import org.example.card.ccg.rogue.spell.Preparation;
import org.example.card.ccg.rogue.spell.Shadowstep;
import org.example.card.ccg.vampire.amulet.BloodfedFlowerbed;
import org.example.card.ccg.vampire.amulet.RestlessParish;
import org.example.card.ccg.vampire.follow.AmblingWraith;
import org.example.card.ccg.vampire.follow.BriaredVampire;
import org.example.card.ccg.vampire.follow.ParaceliseDemonOfGreed;
import org.example.card.ccg.vampire.follow.ShowdownDemon;
import org.example.card.ccg.vampire.spell.*;
import org.example.card.dota.equipment.*;
import org.example.card.dota.follow.*;
import org.example.card.dota.spell.RoosterCrow;
import org.example.card.dota.spell.TeleportToBattleGround;
import org.example.card.other.rule.amulet.MahjongTable;
import org.example.card.other.test.folow.TestFollow;
import org.example.card.paripi.Kongming;
import org.example.card.paripi.follow.CallingOtaku;
import org.example.card.paripi.follow.DiscoZombie;
import org.example.card.paripi.follow.LowKeyTalentScouts;
import org.example.card.paripi.follow.OpeningAct;
import org.example.game.Leader;
import org.example.morecard.genshin.LittlePrincess;
import org.example.morecard.genshin.amulet.FakeSky;
import org.example.morecard.genshin.follow.Diluc;
import org.example.morecard.genshin.follow.Keaya;
import org.example.morecard.genshin.follow.Sucrose;
import org.example.morecard.genshin.spell.*;
import org.example.system.Database;
import org.example.system.util.Maps;

import java.util.ArrayList;
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

            MahjongTable.class, LabRecruiter.class, Preparation.class, Shadowstep.class, Derivant.AnalyzingArtifact.class,
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
            Rhinoceroach.class, FlowerOfFairies.class, FlowerOfFairies.class, ForestSymphony.class, Chronos.class,

            WoodOfBrambles.class, WoodOfBrambles.class, PixieMischief.class, AncientElf.class, AncientElf.class,
            FairyWhisperer.class,FairyWhisperer.class, InsectLord.class,InsectLord.class, ErosiveAnnihilation.class,

            PhantombloomPredator.class,PhantombloomPredator.class,QueenOfTheForest.class,QueenOfTheForest.class,NaturesGuidance.class,
            AirboundBarrage.class, AirboundBarrage.class, AirboundBarrage.class, ForestGenesis.class, ForestGenesis.class
        ));
        deckLeader.put("妖精", Alisa.class);
        decks.put("虫妖",List.of(
            SpringGreenProtection.class, SpringGreenProtection.class, SpringGreenProtection.class,WoodOfBrambles.class, WoodOfBrambles.class,
            QueenOfTheForest.class, QueenOfTheForest.class, QueenOfTheForest.class,PredatoryMight.class, PredatoryMight.class,
            Rhinoceroach.class, Rhinoceroach.class, Rhinoceroach.class,PixieMischief.class, PixieMischief.class,
            GoblinMage.class, GoblinMage.class, GoblinMage.class,AncientElf.class, AncientElf.class,
            AirboundBarrage.class, AirboundBarrage.class, AirboundBarrage.class,FirespriteGrove.class, FirespriteGrove.class,
            FairyCircle.class, FairyCircle.class, FairyCircle.class,AriasWhirlwind.class, AriasWhirlwind.class,
            NaturesGuidance.class, NaturesGuidance.class, NaturesGuidance.class, AngelicSnipe.class, AngelicSnipe.class,
            FlowerOfFairies.class, FlowerOfFairies.class, FlowerOfFairies.class, ArborealCore.class, ArborealCore.class
            ));
        deckLeader.put("虫妖", Alisa.class);
        decks.put("弃牌鬼",List.of(
            BloodfedFlowerbed.class, BloodfedFlowerbed.class, BloodfedFlowerbed.class,RestlessParish.class, RestlessParish.class,RestlessParish.class,
            AmblingWraith.class, AmblingWraith.class, AmblingWraith.class,BriaredVampire.class, BriaredVampire.class,BriaredVampire.class,
            ParaceliseDemonOfGreed.class, ParaceliseDemonOfGreed.class, ParaceliseDemonOfGreed.class,ShowdownDemon.class, ShowdownDemon.class,ShowdownDemon.class,
            EternalContract.class, EternalContract.class, EternalContract.class,FullMoonLeap.class, FullMoonLeap.class,FullMoonLeap.class,
            GiftForBloodkin.class, GiftForBloodkin.class, GiftForBloodkin.class, RazoryClaw.class, RazoryClaw.class,RazoryClaw.class,
            RoomServiceDemon.class, RoomServiceDemon.class, RoomServiceDemon.class,AngelicSnipe.class, AngelicSnipe.class,AngelicSnipe.class,
            AlteredFate.class, AlteredFate.class, MurderousApplication.class,MurderousApplication.class
        ));
        decks.put("死灵术士",List.of(
            CoffinOfTheUnknownSoul.class, CursedCoin.class, DiedParty.class, GloamingTombs.class, HauntedHouse.class,
            BoneFanatic.class, CorpseBride.class, DarkfallenNeophyte.class, Deathmolder.class, HuginnMuninn.class,

            InnGhosthound.class, MalignantHorror.class, MeatGrinder.class, MinoDaydreamingReaper.class,MinoDaydreamingReaper.class,
            NerubianSwarmguard.class, RulenyeScreamingSilence.class, SoulStealer.class, Thoth.class, BloodTap.class,

            BoneAshBibimbap.class, CorralSouls.class, DeathlyTyrantsFeast.class, HearseDrift.class, PlaguedGrain.class,
            SoulConversion.class,TombGuardians.class, TributeSummon.class,Zelgenea.class,MintheOfUnderworld.class
        ));
        deckLeader.put("死灵术士", Luna.class);
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
        decks.put("派对咖",List.of(
            CallingOtaku.class, CallingOtaku.class, CallingOtaku.class,
            DiscoZombie.class, DiscoZombie.class,DiscoZombie.class,
            LowKeyTalentScouts.class, LowKeyTalentScouts.class, LowKeyTalentScouts.class,
            OpeningAct.class, OpeningAct.class, OpeningAct.class
        ));
        deckLeader.put("派对咖", Kongming.class);

        decks.put("野兽猎",List.of(
            InsatiableDevourer.class, InsatiableDevourer.class, IrondeepTrogg.class,IrondeepTrogg.class,Peasant.class,Peasant.class,
            PrinceRenathal.class, SireDenathrius.class, HarpoonGun.class,HarpoonGun.class, BeaststalkerTavish.class, AzsharanSaber.class,
            BarakKodobane.class, HuntsmanAltimor.class, BattyGuest.class,BattyGuest.class,Hydralodon.class,KingKrush.class,
            K90tron.class, K90tron.class, MountainBear.class,MountainBear.class,PetCollector.class,PetCollector.class,
            SpiritPoacher.class, SpiritPoacher.class, TheRatKing.class,WingCommanderIchman.class, DoggieBiscuit.class,DoggieBiscuit.class,
            FrenziedFangs.class, FrenziedFangs.class, RammingMount.class,RammingMount.class, Tracking.class,Tracking.class,
            Aralon.class,Aralon.class, WildSpirits.class, WildSpirits.class
        ));
        deckLeader.put("野兽猎", Rexxar.class);

        decks.put("jojo",List.of(
            EnricoPucci.class, EnricoPucci.class, EnricoPucci.class,
            JolyneCujoh.class,JolyneCujoh.class,JolyneCujoh.class,
            KujoJotaro.class,KujoJotaro.class,KujoJotaro.class
        ));
        decks.put("test",List.of(
            TestFollow.class,TestFollow.class, TestFollow.class,InsatiableDevourer.class,InsatiableDevourer.class,
            TestFollow.class,TestFollow.class, TestFollow.class,InsatiableDevourer.class,InsatiableDevourer.class
        ));
        decks.put("传奇音乐节",List.of(
            ccgm.class,ccgm.class,  dbzwtn.class, fzcf.class,fzcf.class,
            gjdyz.class,jpzr.class, kyj.class, ltstt.class,rcclz.class,
            jzgz.class, kyj.class, ltstt.class, rcclz.class,sysfze.class,
            ygjs.class, ylxs.class, yyjba.class, yyzls.class, zyfn.class,
             yejs.class, zxsh.class, zyfn.class, yejs.class,zxsh.class,
            ccyyt.class, hsyg.class, tbcz.class
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
    public static List describeJson(){
        List<Map<String,Object>> deckInfo = new ArrayList<>();
        decks.forEach((name,cardClassList)-> {
            final List<? extends Card> prototypes = cardClassList.stream().map(Database::getPrototype).toList();
            Leader leader;
            try {
                Class<? extends Leader> leaderClass = DeckPreset.deckLeader.get(name);
                if(leaderClass==null){
                    leaderClass = ThePlayer.class;
                }
                leader = leaderClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            deckInfo.add(Maps.newMap("name", name, "leader", leader,"deck", prototypes));
        });
        return deckInfo;
    }
}
