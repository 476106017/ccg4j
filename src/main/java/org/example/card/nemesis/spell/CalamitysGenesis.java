package org.example.card.nemesis.spell;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.card.Card;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.fairy.follow.Fairy;
import org.example.card.nemesis.follow.MagisterialDreadnought;
import org.example.constant.EffectTiming;
import org.example.game.GameObj;
import org.example.game.Leader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@EqualsAndHashCode(callSuper = true)
@Data
public class CalamitysGenesis extends SpellCard {
    public Integer cost = 1;
    public String name = "灾祸模式";
    public String job = "复仇者";
    public String race = "灾祸";
    public String mark = """
        增加2张正义暴君到牌堆中。回合结束时，随机将1张5费以上随从由牌堆加入手牌。
        """;

    public String subMark = "";

    @Override
    public void play(List<GameObj> targets) {
        super.play(targets);

        List<Card> addCards = new ArrayList<>();
        addCards.add(createCard(MagisterialDreadnought.class));
        addCards.add(createCard(MagisterialDreadnought.class));
        info.thisPlayer().addDeck(addCards);

        // 创建主战者回合结束效果
        Leader.Effect effect = new Leader.Effect(this,EffectTiming.EndTurn, 1,
            player->{
                List<Card> deck = player.getDeck();
                Optional<Card> findCard = deck.stream()
                    .filter(card -> card instanceof FollowCard followCard && followCard.getCost() >= 5)
                    .findAny();
                if(findCard.isEmpty()){
                    info.msg(this.name+"没有找到5费以上随从！");
                }else {
                    info.msg(this.name+"将1张5费以上随从加入手牌！");
                    FollowCard followCard = (FollowCard) findCard.get();
                    List<Card> addList = new ArrayList<>();
                    addList.add(followCard);
                    player.addHand(addList);
                    deck.removeAll(addList);
                }
            });
        ownerPlayer().getLeader().addEffect(effect);
    }
}
