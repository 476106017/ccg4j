package org.example.card.ccg.warlock.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.SpellCard;
import org.example.game.GameObj;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class FelFist extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 6;
    public String name = "邪能拳法";
    public String job = "术士";
    private List<String> race = Lists.ofStr("邪能");
    public String mark = """
        对指定敌方随从造成12点伤害
        如果牌堆为空，则可以指定对方主战者
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
                List<GameObj> targetable = new ArrayList<>();
                if(ownerPlayer().getDeck().isEmpty())
                    targetable.add(info.oppositePlayer().getLeader());
                targetable.addAll(enemyPlayer().getAreaFollowsAsGameObj());
                return targetable;
            },
            true,
            target->info.damageEffect(this,target,12)
        ));
    }

}
