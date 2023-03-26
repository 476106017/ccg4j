package org.example.morecard.genshin.system;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;

@Getter
@Setter
public abstract class ElementBaseFollowCard extends FollowCard {
    public Integer cost = 0;
    private transient Elemental element = Elemental.Universal;
    private transient Elemental attackElement = Elemental.Void;// 普攻附魔
    private transient int burstNeedCharge = 3;

    public abstract void elementalBurst();
    @Override
    public void count() {
        if(getCount() < getBurstNeedCharge()){
            super.count();
            if(getCount() == getBurstNeedCharge()){
                elementalBurst();
                clearCount();
            }
        }
    }
}
