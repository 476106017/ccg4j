package org.example.card.ccg.neutral.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.ArrayList;
import java.util.List;
import org.example.constant.CardRarity;


@Getter
@Setter
public class DrVroom extends FollowCard {

   private CardRarity rarity = CardRarity.RAINBOW;
    private String name = "呜呜博士";
    private Integer cost = 7;
    private int atk = 7;
    private int hp = 7;
    private String job = "中立";
    private List<String> race = Lists.ofStr();
    private String mark = """
        战吼：如果你的牌堆没有卡牌，则获得【护甲】【魔抗】【圣盾】【无视守护】【吸血】【突进】【守护】
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        setPlay(new Play(()->{
            if(ownerPlayer().getDeck().isEmpty()) {
                List<String> keywords = new ArrayList<>();
                keywords.add("护甲");
                keywords.add("魔抗");
                keywords.add("圣盾");
                keywords.add("无视守护");
                keywords.add("吸血");
                keywords.add("突进");
                keywords.add("守护");
                addKeywords(keywords);
            }
        }));
    }
}
