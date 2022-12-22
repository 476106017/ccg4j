package org.example.card.fairy.spell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.FollowCard;
import org.example.card.SpellCard;

import static org.example.constant.CounterKey.TRANSMIGRATION_NUM;

@EqualsAndHashCode(callSuper = true)
@Data
public class EternalForest extends SpellCard {
    public Integer cost = 0;
    public String name = "永恒森林";
    public String job = "妖精";
    public String race = "终极灾厄";
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


}
