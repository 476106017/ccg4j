package org.example.card.anime.chainsawman.follow;

import lombok.Getter;
import lombok.Setter;
import org.example.card.EquipmentCard;
import org.example.card.FollowCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;

@Getter
@Setter
public class ChainsawMan extends FollowCard {
    private int slot = 7;
    private int apposition = 1;
    private String name = "链锯恶魔";
    private Integer cost = 1;
    private int atk = 1;
    private int hp = 3;
    private String job = "链锯人";
    private List<String> race = Lists.ofStr("恶魔");
    private String mark = """
        战吼：增加1张链锯形态到手牌
        击杀时：如果对象是恶魔，则将其沉默并除外；
        超杀:当前装备中的链锯形态可使用次数+1
        """;
    private String subMark = "";

    public void init() {
        setMaxHp(getHp());
        getKeywords().add("恶魔转生");

        setPlay(new Play(() ->
            ownerPlayer().addHand(createCard(ChainsawMode.class))
        ));

        addEffects((new Effect(this,this, EffectTiming.WhenKill,
            obj -> {
                FollowCard followCard = (FollowCard) obj;
                if(followCard.getHp() < 0){
                    if ("链锯模式".equals(getEquipment().getName())) {
                        getEquipment().addCountdown(1);
                    }
                }
                if(followCard.getRace().contains("恶魔")){
                    followCard.purify();
                    getInfo().exile(followCard);
                }
            })));
    }

    @Getter
    @Setter
    public static class ChainsawMode extends EquipmentCard {
        private int apposition = 0;
        public Integer cost = 3;
        public String name = "链锯模式";
        public transient int addAtk = 2;
        public transient int addHp = 2;

        public String job = "链锯人";
        public String mark = """
        指定：链锯人
        """;

        public String subMark = "";

        public void init() {
            setCountdown(1);
            getKeywords().add("突进");
            getKeywords().add("自愈");
            getKeywords().add("重伤");

            setPlay(new Play(
                ()->ownerPlayer().getAreaFollowsAsGameObj().stream()
                    .filter(gameObj -> gameObj.getName().equals("链锯恶魔")).toList(),true,
                gameObjs -> {}));

        }

    }

}
