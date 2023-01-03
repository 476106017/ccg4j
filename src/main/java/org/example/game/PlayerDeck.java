package org.example.game;

import lombok.Getter;
import lombok.Setter;
import org.example.card.Card;
import org.example.card.EquipmentCard;
import org.example.card.FollowCard;
import org.example.system.Database;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public class PlayerDeck {
    Class<? extends Leader> leaderClass;
    List<Class<? extends Card>> activeDeck = new ArrayList<>();
    List<Class<? extends Card>> availableDeck = new ArrayList<>();

    public Leader getLeader(int owner, GameInfo info){
        try {
            Leader leader = leaderClass.getDeclaredConstructor().newInstance();
            leader.setOwner(owner);
            leader.setInfo(info);
            leader.initCounter();
            return leader;
        } catch (NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Card> getActiveDeckInstance(int owner, GameInfo info) {
        List<Card> _return = new ArrayList<>();
        activeDeck.forEach(cardClass->{
            try {
                Card card = cardClass.getDeclaredConstructor().newInstance();
                card.setOwner(owner);
                card.setInfo(info);
                card.initCounter();
                _return.add(card);
            } catch (NoSuchMethodException | InstantiationException |
                     IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        });
        return _return;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder();
        sb.append("【我的收藏】\n暂未开放！\n");
        sb.append("【使用中的主战者】\n");
        try {
            Leader leader = leaderClass.getDeclaredConstructor().newInstance();
            sb.append(leader.getName()).append("\t");
            sb.append("技能：").append(leader.getSkillName())
                .append("（").append(leader.getSkillCost()).append("）\n");
            sb.append(leader.getSkillMark()).append("\n");
            sb.append("超抽效果：").append(leader.getOverDrawMark()).append("\n");
        }catch (Exception e){}
        sb.append("【使用中的牌组】\n");
        sb.append(describeDeck(activeDeck));
        return sb.toString();
    }

    public static String describeDeck(List<Class<? extends Card>> deck){
        StringBuilder sb = new StringBuilder();

        AtomicInteger num = new AtomicInteger(1);
        List<? extends Card> deckCards = deck.stream()
            .map(Database::getPrototype).sorted(Comparator.comparing(Card::getCost)).toList();
        deckCards.forEach(card -> {
            sb.append("<p>");
            sb.append("【").append(num.getAndIncrement()).append("】\t")
                .append(card.getCost()).append("\t")
                .append(card.getType()).append("\t")
                .append(card.getName()).append("\t")
                .append(String.join("/", card.getRace())).append("\t");
            if(card instanceof EquipmentCard equipmentCard && equipmentCard.getCountdown()>0){
                sb.append("可用次数：").append(equipmentCard.getCountdown()).append("\t");
            }
            // region 显示详情
            StringBuilder detail = new StringBuilder();
            if(card instanceof FollowCard followCard)
                detail.append(followCard.getAtk()).append("➹")
                    .append(followCard.getHp()).append("♥");
            detail.append("<div style='text-align:right;float:right;'>")
                .append(String.join("/",card.getRace())).append("</div>\n");
            if(!card.getKeywords().isEmpty())
                detail.append("<b>")
                    .append(String.join(" ", card.getKeywords()))
                    .append("</b>\n");
            detail.append(card.getMark()).append("\n\n");
            detail.append("职业：").append(card.getJob());

            sb.append("""
            <icon class="glyphicon glyphicon-eye-open" style="font-size:18px;"
                    title="%s" data-content="%s"
                    data-container="body" data-toggle="popover"
                      data-trigger="hover" data-html="true"/>
            """.formatted(card.getName(),detail.toString().replaceAll("\\n","<br/>")));
            // endregion
            sb.append("</p>");
        });
        return sb.toString();
    }

}
