package org.example.card;

import org.example.constant.CardType;
import org.example.game.PlayerInfo;

import java.util.ArrayList;

public abstract class SpellCard extends Card{
    public final CardType TYPE = CardType.SPELL;

    @Override
    public String getType() {
        return TYPE.getName();
    }

    // 法术自动施放
    public void autoPlay(){
        PlayerInfo player = ownerPlayer();

        if(!player.getHandPlayable().test(this)){
            info.msgToThisPlayer("由于限制，目前无法自动施放！");
            return;
        }
        info.msg(getNameWithOwner() + "触发自动施放！");
        player.getGraveyard().add(this);
        player.countToGraveyard(1);
        player.getHand().remove(this);

        int temp = player.getDiscoverMax();
        player.setDiscoverMax(1);// 发现效果取随机1张
        getPlay().effect().accept(0,new ArrayList<>());
        player.setDiscoverMax(temp);
    }
}
