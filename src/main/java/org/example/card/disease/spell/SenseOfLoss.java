package org.example.card.disease.spell;

import lombok.Getter;
import lombok.Setter;
import org.example.card.FollowCard;
import org.example.card.SpellCard;
import org.example.constant.EffectTiming;
import org.example.game.Effect;
import org.example.game.Play;
import org.example.system.util.Lists;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class SenseOfLoss extends SpellCard {
    public Integer cost = 1;
    public String name = "丧失感";
    public String job = "疾病";
    private List<String> race = Lists.ofStr("灵魂绑定");
    public String mark = """
        召唤1个1/1的眼泪
        死灵术 10：移除本卡所有关键词并改为召唤1个10/X的眼泪(X是本局对战中召唤其他眼泪的数量)
        """;

    public String subMark = "X等于{}";

    public String getSubMark() {
        return subMark.replaceAll("\\{}",getCount()+"");
    }

    public void init() {
        getKeywords().add("灵魂绑定");
        setPlay(new Play(()->{
            if(ownerPlayer().getGraveyardCount()<10){
                ownerPlayer().summon(createCard(Tear.class));
                count();
            }
            ownerPlayer().costGraveyardCountTo(10,()->{
                getKeywords().clear();
                if(getCount()>0){
                    ownerPlayer().summon(createCard(Tear.class,10,getCount()));
                }
            });
        }));
    }

    @Getter
    @Setter
    public static class Tear extends FollowCard {
        private String name = "眼泪";
        private Integer cost = 0;
        private int atk = 1;
        private int hp = 1;
        private String job = "疾病";
        private List<String> race = Lists.ofStr();
        private String mark = """
        """;
        private String subMark = "";

        public void init() {
            setMaxHp(getHp());
            getKeywords().add("游魂");
        }
    }
}
