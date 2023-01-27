package org.example.card.shadowverse.fairy.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.Lists;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@Getter
@Setter
public class AncientElf extends FollowCard {
    public Integer cost = 3;
    public String name = "远古精灵";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("妖精");
    public String mark = """
        战吼：返回全部我方随从，并获得+X/+X（X是因此效果返回的张数）
        """;
    public String subMark = "";

    public int atk = 2;
    public int hp = 3;

    public AncientElf() {
        setMaxHp(getHp());
        getKeywords().add("守护");
        setPlay(new Play(()->{
            List<AreaCard> areaFollows = ownerPlayer().getAreaFollows();
            AtomicInteger size = new AtomicInteger();
            areaFollows.forEach(areaCard ->{
                if(areaCard != this){
                    size.getAndIncrement();
                    areaCard.backToHand();
                }
            });
            addStatus(size.get(), size.get());
        }));
    }

}
