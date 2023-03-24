package org.example.card.ccg.hunter;

import lombok.Getter;
import lombok.Setter;
import org.example.card.AreaCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.GameObj;
import org.example.game.Leader;
import org.example.system.util.Lists;

import java.util.List;
import java.util.function.Consumer;


@Getter
@Setter
public class BeaststalkerTavishLeader extends Leader {
    private String name = "野兽追猎者塔维什";
    private String job = "猎人";

    private String skillName = "召唤宠物";
    private String skillMark =  """
        召唤1个动物伙伴
        """;
    private int skillCost = 2;

    private boolean needTarget = false;


    private String overDrawMark =  """
        对自己造成疲劳伤害
        """;

    private Consumer<Integer> overDraw = integer -> {
        for (int i = 0; i < integer; i++) {
            ownerPlayer().wearyDamaged();
        }
    };

    @Override
    public void skill(GameObj target) {
        super.skill(target);

        switch ((int)(Math.random()*3)){
            case 0 -> ownerPlayer().summon(createCard(Misha.class));
            case 1 -> ownerPlayer().summon(createCard(Huffer.class));
            case 2 -> ownerPlayer().summon(createCard(Leokk.class));
        }
    }


    @Getter
    @Setter
    public static class Misha extends FollowCard {
        private String name = "米莎";
        private Integer cost = 3;
        private int atk = 4;
        private int hp = 4;
        private String job = "猎人";
        private List<String> race = Lists.ofStr("野兽");
        private String mark = "";
        private String subMark = "";

        public Misha() {
            setMaxHp(getHp());
            getKeywords().add("守护");
        }
    }
    @Getter
    @Setter
    public static class Huffer extends FollowCard {
        private String name = "霍弗";
        private Integer cost = 3;
        private int atk = 4;
        private int hp = 2;
        private String job = "猎人";
        private List<String> race = Lists.ofStr("野兽");
        private String mark = "";
        private String subMark = "";

        public Huffer() {
            setMaxHp(getHp());
            getKeywords().add("疾驰");
        }
    }
    @Getter
    @Setter
    public static class Leokk extends FollowCard {
        private String name = "雷欧克";
        private Integer cost = 3;
        private int atk = 2;
        private int hp = 4;
        private String job = "猎人";
        private List<String> race = Lists.ofStr("野兽");
        private String mark = """
            入场时：我方全体随从+1/+0
            我方召唤时：使其+1/+0
            """;
        private String subMark = "";

        public Leokk() {
            setMaxHp(getHp());
            addEffects((new Effect(this,this, EffectTiming.Entering, obj->{
                ownerPlayer().getAreaFollowsAsFollow().forEach(p->p.addStatus(1,0));
            })));
            addEffects((new Effect(this,this, EffectTiming.WhenSummon, obj->{
                List<AreaCard> summonedCards = (List<AreaCard>) obj;
                summonedCards.forEach(p->{
                    if(p instanceof FollowCard followCard)
                        followCard.addStatus(1,0);
                });
            })));
        }
    }
}
