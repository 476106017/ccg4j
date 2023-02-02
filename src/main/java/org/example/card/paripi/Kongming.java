package org.example.card.paripi;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.genshin.system.ElementBaseFollowCard;
import org.example.card.genshin.system.ElementCostSpellCard;
import org.example.card.genshin.system.Elemental;
import org.example.card.genshin.system.ElementalDamage;
import org.example.constant.EffectTiming;
import org.example.game.*;
import org.example.system.Lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Getter
@Setter
public class Kongming extends Leader {
    private String name = "诸葛孔明";
    private String job = "派对咖";

    private String Mark = """
        每当使用1次战吼，便增加1点派对热度
        """;

    // 超抽效果
    private String overDrawMark = """
        增加疲劳值对应的派对热度，但疲劳为10时输掉游戏（夜越深，人就越精神！）
        """;
    private Consumer<Integer> overDraw = integer -> {};

    private String skillName = "切克闹！";
    private String skillMark =  """
        增加3点派对热度，结束回合
        """;
    private int skillCost = 2;

    private int partyHot = 0;
    @Override
    public void init() {
    }

    @Override
    public List<GameObj> targetable() {
        List<GameObj> targetable = super.targetable();
        targetable.addAll(ownerPlayer().getHand());
        return targetable;
    }

    @Override
    public void skill(GameObj target) {
        super.skill(target);
        Card targetCard = (Card) target;
    }
}
