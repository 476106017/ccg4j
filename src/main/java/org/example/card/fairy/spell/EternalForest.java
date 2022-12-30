package org.example.card.fairy.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.example.constant.CounterKey.TRANSMIGRATION_NUM;


@Getter
@Setter
public class EternalForest extends SpellCard {
    public Integer cost = 0;
    public String name = "永恒森林";
    public String job = "妖精";
    private List<String> race = Lists.ofStr("终极灾厄");
    public String mark = """
        揭示:回合开始时
        主战者增加10点血上限。
        如果轮回数小于30个，则将1张永恒森林洗入牌堆；
        如果轮回数大于30个，则赢得胜利；
        """;

    public String subMark = "轮回数：{count}个";

    public String getSubMark() {
        return subMark.replaceAll("\\{count}",ownerPlayer().getCount(TRANSMIGRATION_NUM)+"");
    }

    public EternalForest() {

        getPlays().add(new Card.Event.Play(() -> {
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
