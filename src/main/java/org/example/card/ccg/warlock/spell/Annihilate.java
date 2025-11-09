package org.example.card.ccg.warlock.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class Annihilate extends SpellCard {

   private CardRarity rarity = CardRarity.BRONZE;
    public Integer cost = 2;
    public String name = "灭杀";
    public String job = "术士";
    private List<String> race = Lists.ofStr("邪能");
    public String mark = """
        对指定敌方随从造成15点伤害
        过量的伤害由己方主战者承受
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->
                enemyPlayer().getAreaFollowsAsGameObj()
            ,
            true,
            target->{
                FollowCard followCard = (FollowCard) target;
                info.damageEffect(this,followCard,15);
                if(followCard.getHp()<0){
                    info.damageEffect(this,ownerLeader(),-followCard.getHp());
                }
            }
        ));
    }

}
