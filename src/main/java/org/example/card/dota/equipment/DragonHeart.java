package org.example.card.dota.equipment;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Damage;
import org.example.game.Effect;
import org.example.game.Play;

import static org.example.constant.CounterKey.PLAY_NUM;

@Getter
@Setter
public class DragonHeart extends EquipmentCard {
    public Integer cost = 4;
    public String name = "魔龙之心";
    public int addAtk = 0;
    public int addHp = 6;
    public String job = "dota";
    public String mark = """
        装备对象在己方回合结束时，回复X点生命（X是本回合使用的卡牌数）
        """;

    public String subMark = "X等于{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}", ownerPlayer().getCount(PLAY_NUM)+"");
    }

    public DragonHeart() {
        setPlay(new Play(
            ()->ownerPlayer().getAreaFollowsAsGameObj(),true,
            gameObj -> {
                gameObj.addEffects(new Effect(this,gameObj,EffectTiming.EndTurn,()->{
                    FollowCard followCard = (FollowCard) gameObj;
                    followCard.heal(followCard.ownerPlayer().getCount(PLAY_NUM));
                }));
            }));
    }
}