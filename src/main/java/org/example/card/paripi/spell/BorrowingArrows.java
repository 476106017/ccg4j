package org.example.card.paripi.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.card.paripi.Kongming;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import org.example.constant.CardRarity;

@Getter
@Setter
public class BorrowingArrows extends SpellCard {

   private CardRarity rarity = CardRarity.SILVER;
    public Integer cost = 3;
    public String name = "草船借箭";
    public String job = "派对咖";
    private List<String> race = Lists.ofStr();
    public String mark = """
        敌方全体-1攻击力
        召唤1个生命值等于敌方随从数量的草船
        派对狂欢 3：并使草船获得【剧毒】
        """;

    public String subMark = "";


    public void init() {
        setPlay(new Play(()->{
            List<FollowCard> follows = enemyPlayer().getAreaFollowsAsFollow();
            follows.forEach(followCard -> followCard.addStatus(-1,0));
            if(follows.isEmpty())return;

            GrassBoat boat = createCard(GrassBoat.class);
            boat.setMaxHp(follows.size());
            boat.setHp(follows.size());
            ownerPlayer().summon(boat);
            if(ownerLeader() instanceof Kongming kongming){
                kongming.costPartyHotTo(3,()->boat.addKeyword("剧毒"));
            }
        }));
    }

    @Getter
    @Setter
    public static class GrassBoat extends FollowCard {

        private CardRarity rarity = CardRarity.BRONZE;
        private String name = "草船";
        private Integer cost = 1;
        private int atk = 0;
        private int hp = 1;
        private String job = "派对咖";
        private List<String> race = Lists.ofStr();
        private String mark = """
        """;
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
            getKeywords().add("守护");
        }
    }

}
