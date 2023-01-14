package org.example.card.genshin;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;

@Getter
@Setter
public abstract class ElementBaseFollowCard extends FollowCard {
    public Integer cost = 0;
    private Elemental element = Elemental.Universal;
    private int burstNeedCharge = 3;

    public abstract void elementalBurst();
    @Override
    public void count() {
        super.count();
        if(getCount() == getBurstNeedCharge()){
            elementalBurst();
        }
    }
}
