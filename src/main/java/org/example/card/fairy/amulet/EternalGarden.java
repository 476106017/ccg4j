package org.example.card.fairy.amulet;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.FollowCard;

import java.util.List;

import static org.example.constant.CounterKey.PLAY_NUM;


@Getter
@Setter
public class EternalGarden extends AmuletCard {

    public Integer cost = 5;

    public String name = "永恒庭园";
    public String job = "妖精";
    private List<String> race = List.of("庭园");
    public String mark = """
        回合结束时：回复X点生命，并且轮回X：妖精随从卡（X是本回合使用卡牌张数）
        """;
    public String subMark = "X等于{playNum}";

    public String getSubMark() {
        return subMark.replaceAll("\\{playNum}", ownerPlayer().getCount(PLAY_NUM)+"");
    }


    public EternalGarden() {
        getEffectEnds().add(new Event.EffectEnd(()->{
            Integer x = ownerPlayer().getCount(PLAY_NUM);
            ownerPlayer().heal(x);

            ownerPlayer().transmigration(card ->
                card instanceof FollowCard followCard && "妖精".equals(followCard.getJob()),x);
        }));
    }

}
