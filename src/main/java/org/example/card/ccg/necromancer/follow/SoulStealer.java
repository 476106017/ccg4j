package org.example.card.ccg.necromancer.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SoulStealer extends FollowCard {
    private String name = "窃魂者";
    private Integer cost = 8;
    private int atk = 5;
    private int hp = 5;
    private String job = "死灵术士";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：破坏所有随从，他们全部进入你的墓地
        """;
    private String subMark = "";

    public SoulStealer() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            List<AreaCard> enemyFollows = enemyPlayer().getAreaFollows();
            List<AreaCard> all = new ArrayList<>(enemyFollows);
            all.addAll(ownerPlayer().getAreaFollows());
            destroy(all);

            List<Card> enemyCards = enemyFollows.stream().map(areaCard -> (Card) areaCard).toList();
            enemyPlayer().getGraveyard().removeAll(enemyCards);
            enemyCards.forEach(GameObj::changeOwner);
            ownerPlayer().addGraveyard(enemyCards);

        }));
    }
}