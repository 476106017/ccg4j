package org.example.card.ccg.vampire.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.Card;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class RoomServiceDemon extends SpellCard {
    public Integer cost = 1;
    public String name = "客房服务的恶魔";
    public String job = "吸血鬼";
    private List<String> race = Lists.ofStr("宴乐");
    public String mark = """
        舍弃1张自己的手牌。 随机给予1个敌方的从者4点伤害，并给予敌方的主战者2点伤害
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(
            ()->ownerPlayer().getHandAsGameObjBy(card ->card!=this ),
            true,
            target->{
                ownerPlayer().abandon((Card) target);
                final AreaCard areaRandomFollow = enemyPlayer().getAreaRandomFollow();
                if(areaRandomFollow!=null)info.damageEffect(this,areaRandomFollow,4);

                info.damageEffect(this,enemyLeader(),2);

            }));
    }

}
