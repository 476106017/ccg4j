package org.example.card.fairy.spell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.nemesis.spell.CalamitysEnd;
import org.example.game.GameObj;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.TRANSMIGRATION_NUM;

@EqualsAndHashCode(callSuper = true)
@Data
public class EternalForest extends SpellCard {
    public Integer cost = 0;
    public String name = "永恒森林";
    public String job = "妖精";
    private List<String> race = List.of("终极灾厄");
    public String mark = """
        揭示:回合开始时
        主战者增加10点血上限。
        如果轮回数小于30个，则增加1张永恒森林到牌堆中；
        如果轮回数大于30个，则赢得胜利；
        """;

    public String subMark = "轮回数：{count}个";

    public String getSubMark() {
        return subMark.replaceAll("\\{count}",ownerPlayer().getCount(TRANSMIGRATION_NUM)+"");
    }

    public EternalForest() {

        getPlays().add(new Card.Event.Play(ArrayList::new,0,
            gameObjs -> {
                ownerPlayer().addHpMax(10);

                long count = ownerPlayer().getCount(TRANSMIGRATION_NUM);
                if(count < 30){
                    List<Card> addCards = new ArrayList<>();
                    addCards.add(createCard(EternalForest.class));
                    ownerPlayer().addDeck(addCards);
                }else {
                    info.gameset(ownerPlayer());
                }
            }
        ));

        getInvocationBegins().add(new Card.Event.InvocationBegin(
            ()->true,
            ()->{}
        ));
    }
}
