package org.example.card.ccg.nemesis.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.ccg.nemesis.Yuwan;
import org.example.game.Play;
import org.example.system.Database;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
public class Mechanization extends SpellCard {
    public Integer cost = 0;
    public String name = "自动化";
    public String job = "复仇者";
    private List<String> race = Lists.ofStr("");
    public String mark = """
        ‧解析的创造物
        ‧古老的创造物
        ‧神秘的创造物
        ‧绚烂的创造物
        发现其中2种，各增加2张到牌堆中。
        """;

    public String subMark = "";
    public void init() {

        setPlay(new Play(
            ()->{
                ownerPlayer().discoverCard(
                    List.of(Database.getPrototype(Yuwan.AnalyzingArtifact.class),
                        Database.getPrototype(Yuwan.AncientArtifact.class),
                        Database.getPrototype(Yuwan.MysticArtifact.class),
                        Database.getPrototype(Yuwan.RadiantArtifact.class)),
                    prototype -> {
                        List<Card> addCards = new ArrayList<>();
                        addCards.add(prototype.copyBy(ownerPlayer()));
                        addCards.add(prototype.copyBy(ownerPlayer()));
                        ownerPlayer().addDeck(addCards);


                        final List<Card> prototypeList = new ArrayList<>(List.of(Database.getPrototype(Yuwan.AnalyzingArtifact.class),
                            Database.getPrototype(Yuwan.AncientArtifact.class),
                            Database.getPrototype(Yuwan.MysticArtifact.class),
                            Database.getPrototype(Yuwan.RadiantArtifact.class)));
                        prototypeList.removeIf(p->p.getClass()==prototype.getClass());
                        ownerPlayer().discoverCard(prototypeList,
                            prototype2 ->  {
                                List<Card> addCards2 = new ArrayList<>();
                                addCards2.add(prototype2.copyBy(ownerPlayer()));
                                addCards2.add(prototype2.copyBy(ownerPlayer()));
                                ownerPlayer().addDeck(addCards2);
                            });
                    });
            }));
    }

}
