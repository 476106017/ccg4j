package org.example.card.fairy.amulet;

import lombok.Data;
import org.example.card.AmuletCard;
import org.example.card.Card;
import org.example.card.FollowCard;

import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ForestSymphony extends AmuletCard {

    public Integer cost = 1;

    public String name = "森林交响乐";
    public String job = "妖精";
    public String race = "乐谱";
    public String mark = """
        回合结束时：如果场上有4个名字不同的随从，则全部+2/+2
        只有10秒的回合无法打出
        """;
    public String subMark = "";


    @Override
    public void effectEnd() {
        if(ownerPlayer().isShortRope()) return;
        Set<String> nameSet = ownerPlayer().getArea().stream()
            .filter(areaCard -> areaCard instanceof FollowCard)
            .map(Card::getName).collect(Collectors.toSet());
        if(nameSet.size()==4){
            info.msg("场上响起了森林的交响乐，随从们受到了激励！");
            ownerPlayer().getArea().forEach(areaCard -> {
                if(areaCard instanceof FollowCard followCard){
                    followCard.changeStatus(2,2);
                }
            });
            death();
        }

    }
}
