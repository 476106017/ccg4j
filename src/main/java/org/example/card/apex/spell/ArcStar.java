package org.example.card.apex.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AmuletCard;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class ArcStar extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 3;
    public String name = "电弧星";
    public String job = "APEX";
    private List<String> race = Lists.ofStr();
    public String mark = """
        造成1点伤害
        """;

    public String subMark = "";
    public int target = 1;
    public void init() {
        getKeywords().add("速攻");
        setPlay(new Play(()->{
            List<GameObj> targetable = new ArrayList<>();
            targetable.add(enemyPlayer().getLeader());
            targetable.addAll(info.getAreaFollowsAsGameObj());
            targetable.add(ownerPlayer().getLeader());
            return targetable;
        },
            true,
            target-> info.damageEffect(this, target, 1)
        ));
    }

}
